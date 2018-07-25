package com.feizi.starter.util;

import com.feizi.starter.entity.MessageData;
import org.apache.rocketmq.common.message.Message;

/**
 * 消息工具类
 * Created by feizi on 2018/6/29.
 */
public class MessageUtils {

    /**
     * 包装信息
     * @param topic topic订阅主题
     * @param msgData 消息内容
     * @return
     */
    public static Message wrapMessage(String topic, MessageData<?> msgData){
        // 对象转json字符串
        String jsonStr = JsonUtils.obj2JsonStr(msgData);
        // json字符串转byte字节数组
        return new Message(topic, StringUtils.str2ByteArr(jsonStr));
    }
}
