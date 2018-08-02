package com.feizi.starter.core;

/**
 * MQ消费者监听
 * Created by feizi on 2018/6/26.
 */
public interface RocketMqConsumerListener<T> {

    /**
     * 监听到消息进行消费
     * @param message MQ消息
     */
    boolean consume(T message);
}
