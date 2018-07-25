package com.feizi.starter.core;

import org.springframework.beans.factory.DisposableBean;

/**
 * MQ消费者监听器容器类
 * Created by feizi on 2018/6/26.
 */
public interface RocketMqConsumerListenerContainer extends DisposableBean {

    /**
     * 注册MQ消费者监听器
     * @param mqConsumerListener
     */
    void registerConsumerListener(RocketMqConsumerListener<?> mqConsumerListener);
}
