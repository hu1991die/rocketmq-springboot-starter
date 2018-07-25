package com.feizi.starter.enums;

/**
 * 消费模式
 * Created by feizi on 2018/6/26.
 */
public enum ConsumeMode {

    /**
     * 多线程并发消费（无序）
     */
    CONCURRENTLY,

    /**
     * 单线程顺序消费（有序）, 一个队列，一个线程
     */
    ORDERLY;
}
