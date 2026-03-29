package com.github.listen_to_me.common.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.github.listen_to_me.common.enumeration.RedisKey;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Slf4j
@Component
public class RedisUtils {

    private static RedisTemplate<String, Object> redisTemplate;

    @Resource
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        RedisUtils.redisTemplate = redisTemplate;
    }

    public static void set(RedisKey redisKey, String suffix, Object value) {
        String key = redisKey.join(suffix);
        log.debug("写入缓存 - Key: {}, 有效期: {} {}", key, redisKey.getExpire(), redisKey.getUnit());
        redisTemplate.opsForValue().set(key, value, redisKey.getExpire(), redisKey.getUnit());
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(RedisKey redisKey, String suffix) {
        String key = redisKey.join(suffix);
        T value = (T) redisTemplate.opsForValue().get(key);
        log.debug("读取缓存 - Key: {}, 是否命中: {}", key, value != null);
        return value;
    }

    public static Boolean delete(RedisKey redisKey, String suffix) {
        String key = redisKey.join(suffix);
        log.debug("删除缓存 - Key: {}", key);
        return redisTemplate.delete(key);
    }

    public static void setJson(RedisKey redisKey, String suffix, Map<String, Object> map) {
        String json = JSONUtil.toJsonStr(map);
        set(redisKey, suffix, json);
    }

    public static Map<String, Object> getJson(RedisKey redisKey, String suffix) {
        String json = get(redisKey, suffix);
        JSONObject jsonObj = JSONUtil.parseObj(json);
        return jsonObj.getRaw();
    }
}
