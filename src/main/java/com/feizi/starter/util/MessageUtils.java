package com.feizi.starter.util;

import com.feizi.starter.entity.MessageData;
import org.apache.rocketmq.common.message.Message;

import java.util.UUID;

/**
 * 消息工具类
 * Created by feizi on 2018/6/29.
 */
public class MessageUtils {

    /**
     * 包装信息
     * @param topic topic订阅主题
     * @param tags 标签，子主题, 可用于区分不同的业务操作
     * @param keys 每个消息在业务层面的唯一标识码
     * @param msgData 消息内容
     * @return
     */
    public static Message wrapMessage(String topic, String tags, String keys, MessageData<?> msgData){
        // 对象转json字符串
        String jsonStr = JsonUtils.obj2JsonStr(msgData);
        // json字符串转byte字节数组
        return new Message(topic, tags, keys, StringUtils.str2ByteArr(jsonStr));
    }

    /**
     * 构建MQ消息
     * @param topic 订阅topic主题
     * @param tags 标签，子主题, 可用于区分不同的业务操作
     * @param keys 每个消息在业务层面的唯一标识码
     * @param data 消息内容
     * @return
     */
    public static  <T> Message buildMessage(String topic, String tags, String keys, T data){
        MessageData<T> messageData = new MessageData<>();
        messageData.setUuid(UUID.randomUUID().toString());
        messageData.setTimestamp(System.currentTimeMillis());
        messageData.setData(data);

        Message message = wrapMessage(topic, tags, keys, messageData);
        return message;
    }
}
