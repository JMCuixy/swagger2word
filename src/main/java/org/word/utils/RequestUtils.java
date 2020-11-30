package org.word.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;


/**
 * @author : ivenhan
 * @date : 2020/10/16
 */
public class RequestUtils {

    public static void validateRequestKey(Map<String, Map> content) throws JsonProcessingException {
		Map<String, Map> applicationJSON = content.get("application/json");
        if (applicationJSON == null) {
        	throw new JsonProcessingException("content字段 缺少 application/json 字段") {};
        }

        Map<String, Map> schema = applicationJSON.get("schema");
        if (schema == null) {
        	throw new JsonProcessingException("content字段 application/json 缺少 schema 字段") {};
        }

        if (schema.get("type") == null) {
        	throw new JsonProcessingException("content字段 application/json 字段 schema 字段 缺少 type字段") {};
        }
    }
}