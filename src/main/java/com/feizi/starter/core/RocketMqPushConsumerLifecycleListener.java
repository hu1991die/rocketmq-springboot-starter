package com.feizi.starter.core;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;

/**
 * MQ推模式消费者生命周期监听器
 * Created by feizi on 2018/6/26.
 */
public interface RocketMqPushConsumerLifecycleListener extends RocketMqConsumerLifecycleListener<DefaultMQPushConsumer> {
}
