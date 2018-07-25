package com.feizi.starter.annotation;

import com.feizi.starter.enums.ConsumeMode;
import com.feizi.starter.enums.SelectorType;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RocketMQ消费者注解
 * Created by feizi on 2018/6/26.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RocketMqConsumer {

    /**
     * 消费者组
     * @return
     */
    String consumerGroup();

    /**
     * 订阅topic主题
     * @return
     */
    String topic();

    /**
     * 消息选择器类型，默认：tag
     * @return
     */
    SelectorType selectorType() default SelectorType.TAG;

    /**
     * 选择器表达式，默认：“*”
     * @return
     */
    String selectorExpress() default "*";

    /**
     * 消费模式，默认：多线程并发消费
     * @return
     */
    ConsumeMode consumeMode() default ConsumeMode.CONCURRENTLY;

    /**
     * 消费方式，默认：集群消费
     * @return
     */
    MessageModel messageMode() default MessageModel.CLUSTERING;

    /**
     * 最大消费者线程数量，默认：64
     * @return
     */
    int consumeThreadMax() default 64;
}
