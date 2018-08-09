package com.feizi.starter.config;

import com.feizi.starter.RocketMqSendTemplate;
import com.feizi.starter.annotation.RocketMqConsumer;
import com.feizi.starter.constant.RocketMqConstants;
import com.feizi.starter.core.DefaultRocketMqListenerContainer;
import com.feizi.starter.core.RocketMqConsumerListener;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.MQClientAPIImpl;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RocketMQ自动配置类
 * Created by feizi on 2018/6/26.
 */
@Configuration
@EnableConfigurationProperties(RocketMqProperties.class)
@ConditionalOnClass(MQClientAPIImpl.class)
@Order
public class RocketMqAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMqAutoConfiguration.class);

    @Resource
    private RocketMqProperties rocketMqProperties;

    /**
     * 生产者
     * @return
     */
    @Bean
    @ConditionalOnClass(DefaultMQProducer.class)
    @ConditionalOnMissingBean(DefaultMQProducer.class)
    @ConditionalOnProperty(prefix = "spring.rocketmq", value = {"name-server", "producer-config.group"})
    public DefaultMQProducer mqProducer(){
        /**
         * 设置生产者参数
         */
        //获取MQ生产者配置
        RocketMqProperties.RocketMqProducerConfig producerConfig = rocketMqProperties.getProducerConfig();

        //获取nameServer地址
        String nameServer = rocketMqProperties.getNameServer();
        Assert.hasText(nameServer, "[spring.rocketmq.name-server] must not be null");

        //获取生产者组
        String producerGroup = producerConfig.getGroup();
        Assert.hasText(producerGroup, "[spring.rocketmq.producer-config.group] must not be null");

        LOGGER.info("rocketMq name-server: {}, producer-config.group: {}", nameServer, producerGroup);

        //实例化一个生产者
        DefaultMQProducer producer = new DefaultMQProducer(producerGroup);
        //设置nameServer寻址地址
        producer.setNamesrvAddr(nameServer);

        //发送消息超时时间
        producer.setSendMsgTimeout(producerConfig.getSendMsgTimeout());
        //发送同步消息失败后重试次数
        producer.setRetryTimesWhenSendFailed(producerConfig.getRetryTimesWhenSendFailed());
        //发送异步消息失败后重试次数
        producer.setRetryTimesWhenSendAsyncFailed(producerConfig.getRetryTimesWhenSendAsyncFailed());
        //单条消息的最大容量：默认4M
        producer.setMaxMessageSize(producerConfig.getMaxMessageSize());
        //消息压缩阈值：超过4k会被压缩
        producer.setCompressMsgBodyOverHowmuch(producerConfig.getCompressMsgBodyOverHowmuch());
        //发送失败是否立即切换至其他broker
        producer.setRetryAnotherBrokerWhenNotStoreOK(producerConfig.isRetryAnotherBrokerWhenNotStoreOk());
        /*关闭VIP通道*/
        producer.setVipChannelEnabled(false);

        return producer;
    }

    /**
     * 发送模板
     * @param producer
     * @return
     */
    @Bean(destroyMethod = "destroy")
    @ConditionalOnClass(DefaultMQProducer.class)
    @ConditionalOnMissingBean(name = "rocketMQSendTemplate")
    public RocketMqSendTemplate rocketMQSendTemplate(DefaultMQProducer producer){
        RocketMqSendTemplate rocketMqSendTemplate = new RocketMqSendTemplate();
        rocketMqSendTemplate.setProducer(producer);

        return rocketMqSendTemplate;
    }

    /**
     * 监听器容器配置类
     */
    @Configuration
    @ConditionalOnClass(DefaultMQPushConsumer.class)
    @EnableConfigurationProperties(RocketMqProperties.class)
    @ConditionalOnProperty(prefix = "spring.rocketmq", value = "name-server")
    @Order
    public static class ListenerContainerConfiguration implements ApplicationContextAware, InitializingBean{
        private static final Logger LOGGER = LoggerFactory.getLogger(ListenerContainerConfiguration.class);

        /**
         * 上下文
         */
        private ConfigurableApplicationContext applicationContext;

        /**
         * 计数器
         */
        private AtomicLong counter = new AtomicLong(0);

        /**
         * 运行环境
         */
        @Resource
        private StandardEnvironment environment;

        /**
         * RocketMq属性配置
         */
        @Resource
        private RocketMqProperties rocketMqProperties;

        public ListenerContainerConfiguration() {
        }

        @Override
        public void setApplicationContext(ApplicationContext context) throws BeansException {
            //设置上下文
            this.applicationContext = (ConfigurableApplicationContext) context;
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            //获取配置了RocketMQMessageListener消费者监听注解的类信息
            Map<String, Object> beans = this.applicationContext.getBeansWithAnnotation(RocketMqConsumer.class);
            if(Objects.nonNull(beans)){
                beans.forEach(this::registerMessageListener);
            }
        }

        /**
         * 注册消息监听器
         * @param beanName bean名称
         * @param bean bean对象
         */
        private void registerMessageListener(String beanName, Object bean){
            Class<?> clazz = AopUtils.getTargetClass(bean);

            if(!RocketMqConsumerListener.class.isAssignableFrom(bean.getClass())){
                throw new IllegalStateException("rocketMq " + clazz + " is not instance of " + RocketMqConsumerListener.class.getName());
            }

            RocketMqConsumerListener rocketMqConsumerListener = (RocketMqConsumerListener) bean;
            //获取注解
            RocketMqConsumer annotation = clazz.getAnnotation(RocketMqConsumer.class);
            BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.rootBeanDefinition(DefaultRocketMqListenerContainer.class);
            //nameserver寻址
            beanBuilder.addPropertyValue(RocketMqConstants.PROP_NAMESERVER, rocketMqProperties.getNameServer());
            //topic主题
            beanBuilder.addPropertyValue(RocketMqConstants.PROP_TOPIC, environment.resolvePlaceholders(annotation.topic()));
            //消费者组
            beanBuilder.addPropertyValue(RocketMqConstants.PROP_CONSUMER_GROUP, environment.resolvePlaceholders(annotation.consumerGroup()));
            //消费模式（并发消费或顺序消费）
            beanBuilder.addPropertyValue(RocketMqConstants.PROP_CONSUME_MODE, annotation.consumeMode());
            //最大消费线程数
            beanBuilder.addPropertyValue(RocketMqConstants.PROP_CONSUME_THREAD_MAX, annotation.consumeThreadMax());
            //消费方式（集群消费或广播消费）
            beanBuilder.addPropertyValue(RocketMqConstants.PROP_MESSAGE_MODEL, annotation.messageMode());
            //选择器表达式
            beanBuilder.addPropertyValue(RocketMqConstants.PROP_SELECTOR_EXPRESS, environment.resolvePlaceholders(annotation.selectorExpress()));
            //选择器类型《tag）
            beanBuilder.addPropertyValue(RocketMqConstants.PROP_SELECTOR_TYPE, annotation.selectorType());
            //消费者监听器
            beanBuilder.addPropertyValue(RocketMqConstants.PROP_ROCKETMQ_CONSUMER_LISTENER, rocketMqConsumerListener);
            //销毁方法
            beanBuilder.setDestroyMethodName(RocketMqConstants.METHOD_DESTROY);

            String containerBeanName = String.format("%s_%s", DefaultRocketMqListenerContainer.class.getName(), counter.incrementAndGet());
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getBeanFactory();
            beanFactory.registerBeanDefinition(containerBeanName, beanBuilder.getBeanDefinition());

            DefaultRocketMqListenerContainer container = beanFactory.getBean(containerBeanName, DefaultRocketMqListenerContainer.class);
            if(!container.isStarted()){
                try {
                    container.start();
                } catch (MQClientException e) {
                    LOGGER.error("rocketMq started container failed. {}", container, e);
                    throw new RuntimeException(e);
                }
            }
            LOGGER.info("rocketMq register consumer listener to container, beanName:{}, containerBeanName:{}", beanName, containerBeanName);
        }
    }
}
