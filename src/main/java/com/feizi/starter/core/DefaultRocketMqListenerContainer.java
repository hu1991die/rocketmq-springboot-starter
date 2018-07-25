package com.feizi.starter.core;

import com.feizi.starter.enums.ConsumeMode;
import com.feizi.starter.enums.SelectorType;
import com.feizi.starter.util.JsonUtils;
import com.feizi.starter.util.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

/**
 * MQ监听容器（消费者）
 * Created by feizi on 2018/6/26.
 */
public class DefaultRocketMqListenerContainer implements InitializingBean, RocketMqConsumerListenerContainer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRocketMqListenerContainer.class);

    /**
     * 暂停当前队列时间
     */
    private long suspendCurrentQueueTimeMillis = 1000;

    /**
     * 再次消费的时候延时级别
     */
    private int delayLevelWhenNextConsume = 0;

    /**
     * 消费者组
     */
    private String consumerGroup;

    /**
     * nameServer寻址
     */
    private String nameServer;

    /**
     * 订阅topic主题
     */
    private String topic;

    /**
     * 消费模式：默认多线程并发消费
     */
    private ConsumeMode consumeMode = ConsumeMode.CONCURRENTLY;

    /**
     * 消息选择器类型，默认：tag
     */
    private SelectorType selectorType = SelectorType.TAG;

    /**
     * 消息选择器表达式，默认：*
     */
    private String selectorExpress = "*";

    /**
     * 消费方式，默认：集群消费
     */
    private MessageModel messageModel = MessageModel.CLUSTERING;

    /**
     * 最大消费者线程数量：64
     */
    private int consumeThreadMax = 64;

    /**
     * 字符集编码格式：UTF-8
     */
    private String charsetUtf8 = "UTF-8";

    /**
     * MQ监听器容器是否启动标识
     */
    private boolean started;

    /**
     * MQ消费者监听器
     */
    private RocketMqConsumerListener rocketMqConsumerListener;

    /**
     * MQ推模式消费者
     */
    private DefaultMQPushConsumer consumer;

    /**
     * 消息类型
     */
    private Class messageType;

    @Override
    public void afterPropertiesSet() throws Exception {
        //启动消费者监听容器
        start();
    }

    @Override
    public void registerConsumerListener(RocketMqConsumerListener<?> mqConsumerListener) {
        this.rocketMqConsumerListener = mqConsumerListener;
    }

    @Override
    public void destroy() throws Exception {
        //销毁消费者监听容器
        this.setStarted(false);
        if(Objects.nonNull(consumer)){
            consumer.shutdown();
        }
        LOGGER.info("rocketMq defaultRocketMQListenerContainer destroyed, {}", this.toString());
    }

    /**
     * 启动MQ监听容器
     */
    public synchronized void start() throws MQClientException {
        //判断容器是否启动
        if(this.isStarted()){
            throw new IllegalStateException("DefaultRocketMQListenerContainer already started. " + this.toString());
        }

        //初始化MQ消费者
        initRocketMQPushConsumer();

        //解析消息类型type
        this.messageType = getMessageType();
        LOGGER.info("rocketMq receive the msgType: {}", messageType.getName());

        //启动消费者
        consumer.start();

        //设置启动标识
        this.setStarted(true);
        LOGGER.info("rocketMq start consumer listener container: {}", this.toString());
    }

    /**
     * 多线程并发消费模式
     */
    public class DefaultMessageListenerConcurrently implements MessageListenerConcurrently{

        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
            for (MessageExt messageExt : msgs) {
                try {
                    LOGGER.info("rocketMq consumer received message: {}", messageExt);
                    long begin = System.currentTimeMillis();

                    //对接收到的MQ消息进行转换
                    Object message = doConvertMessage(messageExt);
                    rocketMqConsumerListener.consume(message);

                    long end = System.currentTimeMillis();
                    LOGGER.info("rocketMq consume msgId: {}, cost: {} ms", messageExt.getMsgId(), (end - begin));
                } catch (Exception e) {
                    LOGGER.warn("rocketMq consume message failed. msg: {}", messageExt, e);

                    //设置下次消费的延时级别
                    context.setDelayLevelWhenNextConsume(delayLevelWhenNextConsume);
                    //稍后重试
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
            //消费成功
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
    }

    /**
     * 单线程顺序消费，一队列，一线程
     */
    public class DefaultMessageListenerOrderly implements MessageListenerOrderly{

        @Override
        public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
            for (MessageExt messageExt : msgs) {
                try {
                    LOGGER.info("rocketMq consumer received message: {}", messageExt);
                    long begin = System.currentTimeMillis();

                    //消费转换后的MQ消息
                    rocketMqConsumerListener.consume(doConvertMessage(messageExt));

                    long end = System.currentTimeMillis();
                    LOGGER.info("rocketMq consume msgId: {}, cost: {} ms", messageExt.getMsgId(), (end - begin));
                } catch (Exception e) {
                    LOGGER.warn("rocketMq consume message failed. msg:{}", messageExt, e);
                    //如果消费失败，则让当前队列暂停一会再消费
                    context.setSuspendCurrentQueueTimeMillis(suspendCurrentQueueTimeMillis);

                    //当前队列暂停消费一会儿
                    return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                }
            }
            //消费成功
            return ConsumeOrderlyStatus.SUCCESS;
        }
    }

    /**
     * 初始化MQ消费者
     */
    private void initRocketMQPushConsumer() throws MQClientException {
        Assert.notNull(rocketMqConsumerListener, "Property 'rocketMqConsumerListener' is required");
        Assert.notNull(consumerGroup, "Property 'consumerGroup' is required");
        Assert.notNull(nameServer, "Property 'nameServer' is required");
        Assert.notNull(topic, "Property 'topic' is required");

        LOGGER.info("rocketMq initRocketMQPushConsumer, rocketMqConsumerListener: {}, consumerGroup: {}, nameServer: {}, topic: {}",
                rocketMqConsumerListener.getClass().getName(), consumerGroup, nameServer, topic);

        /* 设置消费者组 */
        consumer = new DefaultMQPushConsumer(consumerGroup);
        /* 设置nameServer寻址 */
        consumer.setNamesrvAddr(nameServer);
        /* 设置最大消费者线程数 */
        consumer.setConsumeThreadMax(consumeThreadMax);

        if(consumeThreadMax < consumer.getConsumeThreadMin()){
            //以设置的最大消费者线程数为准
            consumer.setConsumeThreadMax(consumeThreadMax);
        }

        /* 设置消费方式, 默认为: 集群消费 */
        consumer.setMessageModel(messageModel);

        // 判断消费选择器
        LOGGER.info("rocketMq selectorType is: {}", selectorType.name());
        switch (selectorType){
            case TAG:
                consumer.subscribe(topic, selectorExpress);
                break;
            default:
                throw new IllegalArgumentException("Property 'selectorType' was wrong...");
        }

        //判断消费方式，顺序消费还是并发消费，并且注册消息监听器
        LOGGER.info("rocketMq consumeMode is: {}", consumeMode.name());
        switch (consumeMode){
            case ORDERLY:
                consumer.registerMessageListener(new DefaultMessageListenerOrderly());
                break;
            case CONCURRENTLY:
                consumer.registerMessageListener(new DefaultMessageListenerConcurrently());
                break;
            default:
                throw new IllegalArgumentException("Property 'consumeMode' was wrong.");
        }

        if(rocketMqConsumerListener instanceof RocketMqPushConsumerLifecycleListener){
            ((RocketMqPushConsumerLifecycleListener) rocketMqConsumerListener).prepareStart(consumer);
        }
    }

    @Override
    public String toString() {
        return "DefaultRocketMQListenerContainer{" +
                "consumerGroup='" + consumerGroup + '\'' +
                ", nameServer='" + nameServer + '\'' +
                ", topic='" + topic + '\'' +
                ", consumeMode=" + consumeMode +
                ", selectorType=" + selectorType +
                ", selectorExpress='" + selectorExpress + '\'' +
                ", messageModel=" + messageModel +
                ", consumeThreadMax=" + consumeThreadMax +
                '}';
    }

    /**
     * 消息转换
     * @param messageExt 扩展类消息
     * @return
     */
    private Object doConvertMessage(MessageExt messageExt){
        //判断接收到的MQ消费类型
        if(Objects.equals(messageType, MessageExt.class)){
           return messageExt;
        }else {
            //将接收到的MQ消息转成字符串（UTF-8）
            String msg = StringUtils.byteArr2Str(messageExt.getBody());
            if(Objects.equals(messageType, String.class)){
                return msg;
            }else {
                // 如果消息不是String类型，则使用objectMapper进行转换，将json转成obj
                return JsonUtils.jsonStr2Obj(msg, messageType);
            }
        }
    }

    /**
     * 获取消息类型
     * @return
     */
    private Class getMessageType(){
        //获取所有的接口定义
        Type[] interfaces = rocketMqConsumerListener.getClass().getGenericInterfaces();
        if(Objects.nonNull(interfaces) && interfaces.length > 0){
            for (Type intf : interfaces) {
                if(intf instanceof ParameterizedType){
                    ParameterizedType parameterizedType = (ParameterizedType) intf;
                    if(Objects.equals(parameterizedType.getRawType(), RocketMqConsumerListener.class)){
                        //获取所有的实际类型参数定义
                        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                        if(Objects.nonNull(actualTypeArguments) && actualTypeArguments.length > 0){
                            return (Class) actualTypeArguments[0];
                        }else {
                            return Object.class;
                        }
                    }
                }
            }
            return Object.class;
        }else {
            return Object.class;
        }
    }

    public long getSuspendCurrentQueueTimeMillis() {
        return suspendCurrentQueueTimeMillis;
    }

    public void setSuspendCurrentQueueTimeMillis(long suspendCurrentQueueTimeMillis) {
        this.suspendCurrentQueueTimeMillis = suspendCurrentQueueTimeMillis;
    }

    public int getDelayLevelWhenNextConsume() {
        return delayLevelWhenNextConsume;
    }

    public void setDelayLevelWhenNextConsume(int delayLevelWhenNextConsume) {
        this.delayLevelWhenNextConsume = delayLevelWhenNextConsume;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getNameServer() {
        return nameServer;
    }

    public void setNameServer(String nameServer) {
        this.nameServer = nameServer;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public ConsumeMode getConsumeMode() {
        return consumeMode;
    }

    public void setConsumeMode(ConsumeMode consumeMode) {
        this.consumeMode = consumeMode;
    }

    public SelectorType getSelectorType() {
        return selectorType;
    }

    public void setSelectorType(SelectorType selectorType) {
        this.selectorType = selectorType;
    }

    public String getSelectorExpress() {
        return selectorExpress;
    }

    public void setSelectorExpress(String selectorExpress) {
        this.selectorExpress = selectorExpress;
    }

    public MessageModel getMessageModel() {
        return messageModel;
    }

    public void setMessageModel(MessageModel messageModel) {
        this.messageModel = messageModel;
    }

    public int getConsumeThreadMax() {
        return consumeThreadMax;
    }

    public void setConsumeThreadMax(int consumeThreadMax) {
        this.consumeThreadMax = consumeThreadMax;
    }

    public String getCharsetUtf8() {
        return charsetUtf8;
    }

    public void setCharsetUtf8(String charsetUtf8) {
        this.charsetUtf8 = charsetUtf8;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public void setRocketMqConsumerListener(RocketMqConsumerListener rocketMqConsumerListener) {
        this.rocketMqConsumerListener = rocketMqConsumerListener;
    }
}
