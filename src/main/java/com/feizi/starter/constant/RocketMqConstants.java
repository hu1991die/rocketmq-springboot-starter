package com.feizi.starter.constant;

/**
 * Created by feizi on 2018/6/26.
 */
public final class RocketMqConstants {
    /**
     * nameserver寻址
     */
    public static final String PROP_NAMESERVER = "nameServer";

    /**
     * 订阅topic主题
     */
    public static final String PROP_TOPIC = "topic";

    /**
     * 消费者组
     */
    public static final String PROP_CONSUMER_GROUP = "consumerGroup";

    /**
     * 消费模式（并发消费或顺序消费）
     */
    public static final String PROP_CONSUME_MODE = "consumeMode";

    /**
     * 最大消费线程数
     */
    public static final String PROP_CONSUME_THREAD_MAX = "consumeThreadMax";

    /**
     * 消费方式（集群消费或广播消费）
     */
    public static final String PROP_MESSAGE_MODEL = "messageModel";

    /**
     * 选择器表达式
     */
    public static final String PROP_SELECTOR_EXPRESS = "selectorExpress";

    /**
     * 选择器类型《tag）
     */
    public static final String PROP_SELECTOR_TYPE = "selectorType";

    /**
     * 消费者监听器
     */
    public static final String PROP_ROCKETMQ_CONSUMER_LISTENER = "rocketMqConsumerListener";

    /**
     * 销毁
     */
    public static final String METHOD_DESTROY = "destroy";
}
