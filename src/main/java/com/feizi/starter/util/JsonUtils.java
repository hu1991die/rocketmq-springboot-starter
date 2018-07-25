package com.feizi.starter.util;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * JSON工具类
 * @author feizi
 * @date 2018/7/25 10:05.
 **/
public class JsonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * obj对象转json字符串
     * @param obj 对象
     * @return
     */
    public static String obj2JsonStr(Object obj) {
        try {
            if (obj == null) {
                return null;
            }
            return objectMapper.writeValueAsString(obj);
        } catch (JsonGenerationException e) {
            LOGGER.error(e.getMessage());
            return null;
        } catch (JsonMappingException e) {
            LOGGER.error(e.getMessage());
            return null;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    /**
     * json字符串转Obj对象（支持泛型的解析）
     * @param jsonStr json字符串
     * @param clz obj对象
     * @param <T> 泛型
     * @return
     */
    public static <T> T jsonStr2Obj(String jsonStr, Class<T> clz) {
        try {
            if(StringUtils.isBlank(jsonStr)){
                return null;
            }
            return objectMapper.readValue(jsonStr, clz);
        } catch (JsonParseException e) {
            LOGGER.error("jsonStr to obj error jsonString=" + jsonStr + " class " + clz.toString(), e);
            return null;
        } catch (JsonMappingException e) {
            LOGGER.error("jsonStr to obj error jsonString=" + jsonStr + " class " + clz.toString(), e);
            return null;
        } catch (IOException e) {
            LOGGER.error("jsonStr to obj error jsonString=" + jsonStr + " class " + clz.toString(), e);
            return null;
        }
    }

    /**
     * json字符串转Obj对象
     * @param jsonStr json字符串
     * @param typeReference 引用类型
     * @param <T> 泛型
     * @return
     */
    public static <T> T jsonStr2Obj(String jsonStr, TypeReference<T> typeReference) {
        try {
            if(StringUtils.isBlank(jsonStr)){
                return null;
            }
            return objectMapper.readValue(jsonStr, typeReference);
        } catch (JsonParseException e) {
            LOGGER.error("jsonString to obj error jsonString=" + jsonStr + " class " + typeReference.toString(), e);
            return null;
        } catch (JsonMappingException e) {
            LOGGER.error("jsonString to obj error jsonString=" + jsonStr + " class " + typeReference.toString(), e);
            return null;
        } catch (IOException e) {
            LOGGER.error("jsonString to obj error jsonString=" + jsonStr + " class " + typeReference.toString(), e);
            return null;
        }
    }

    /**
     * json格式对象转成obj对象
     * @param jsonObj
     * @param typeReference
     * @param <T>
     * @return
     */
    public static <T> T jsonObj2Obj(Object jsonObj, TypeReference<T> typeReference) {
        String jsonStr = "";
        try {
            if(null == jsonObj){
                return null;
            }
            //首先将jsonObj转成json字符串
            jsonStr = obj2JsonStr(jsonObj);
            return objectMapper.readValue(jsonStr, typeReference);
        } catch (JsonParseException e) {
            LOGGER.error("jsonObj to obj error jsonStr=" + jsonStr + " class " + typeReference.toString(), e);
            return null;
        } catch (JsonMappingException e) {
            LOGGER.error("jsonObj to obj error jsonStr=" + jsonStr + " class " + typeReference.toString(), e);
            return null;
        } catch (IOException e) {
            LOGGER.error("jsonObj to obj error jsonStr=" + jsonStr + " class " + typeReference.toString(), e);
            return null;
        }
    }

    /**
     * Model和DTO和VO三者相互转换
     * @param obj Model或者DTO或者Map
     * @param targetClazz DTO.class或者Model.class或者Map.class
     * @param <S> 泛型
     * @param <T> 泛型
     * @return
     */
    public static final <S, T> T transferObj(S obj, Class<T> targetClazz) {
        if(null == obj){
            return null;
        }
        return JSON.parseObject(JSON.toJSONString(obj), targetClazz);
    }

    /**
     * List<Model>和List<DTO>和List<VO>三者相互转换
     * @param list list集合
     * @param targetClazz list集合
     * @param <S> 泛型
     * @param <T> 泛型
     * @return
     */
    public static final <S, T> List<T> transferList(List<S> list, Class<T> targetClazz) {
        if(null != list && list.size() > 0){
            return null;
        }
        return JSON.parseArray(JSON.toJSONString(list), targetClazz);
    }
}
