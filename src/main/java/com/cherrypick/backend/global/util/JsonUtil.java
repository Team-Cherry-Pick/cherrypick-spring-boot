package com.cherrypick.backend.global.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;

public class JsonUtil {

    static public String toJson(HashMap<String, Object> map) {
        ObjectMapper mapper = new ObjectMapper();
        try{
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return map.toString();
        }
    }

}
