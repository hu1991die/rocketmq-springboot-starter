package com.feizi.starter.entity;

import java.io.Serializable;

/**
 * 消息封装实体类
 * Created by feizi on 2018/6/29.
 */
public class MessageData<T> implements Serializable{
    private static final long serialVersionUID = 2992066225590965125L;

    /**
     * UUID唯一标识，用于控制幂等
     */
    private String uuid;

    /**
     * 消息产生时间戳
     */
    private long timestamp;

    /**
     * 消息内容，格式为json字符串
     */
    private T data;

    public MessageData() {
    }

    public MessageData(String uuid, long timestamp, T data) {
        this.uuid = uuid;
        this.timestamp = timestamp;
        this.data = data;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "MessageData{" +
                "uuid='" + uuid + '\'' +
                ", timestamp=" + timestamp +
                ", data=" + data +
                '}';
    }
}
