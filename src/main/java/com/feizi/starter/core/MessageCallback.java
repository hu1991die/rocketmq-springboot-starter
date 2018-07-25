package com.feizi.starter.core;

import org.apache.rocketmq.client.producer.SendResult;

/**
 * 消息回调
 * Created by feizi on 2018/6/26.
 */
public interface MessageCallback {
    void callback(SendResult result);
}
