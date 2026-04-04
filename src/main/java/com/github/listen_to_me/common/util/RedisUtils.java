package com.github.listen_to_me.common.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import com.github.listen_to_me.common.enumeration.RedisKey;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.Set;

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

    /**
     * 向有序集合（ZSet）中添加元素
     *
     * @param redisKey RedisKey枚举
     * @param suffix   后缀
     * @param member   成员
     * @param score    分数（用于排序）
     */
    public static void addToZSet(RedisKey redisKey, String suffix, Object member, double score) {
        String key = redisKey.join(suffix);
        redisTemplate.opsForZSet().add(key, member, score);
        log.debug("ZSet添加 - Key: {}, 成员: {}, 分数: {}", key, member, score);
    }

    /**
     * 获取有序集合（ZSet）倒序排列的成员列表
     *
     * @param redisKey RedisKey枚举
     * @param suffix   后缀
     * @param start    起始索引（0表示第一个）
     * @param end      结束索引（如9表示前10个）
     * @return 成员集合，按分数从高到低排序
     */
    public static Set<Object> getZSetReverseRange(RedisKey redisKey, String suffix, long start, long end) {
        String key = redisKey.join(suffix);
        Set<Object> result = redisTemplate.opsForZSet().reverseRange(key, start, end);
        log.debug("ZSet倒序查询 - Key: {}, 数量: {}", key, result != null ? result.size() : 0);
        return result;
    }

    /**
     * 删除有序集合（ZSet）对应的整个Key
     *
     * @param redisKey RedisKey枚举
     * @param suffix   后缀
     */
    public static void deleteZSet(RedisKey redisKey, String suffix) {
        String key = redisKey.join(suffix);
        redisTemplate.delete(key);
        log.debug("ZSet删除 - Key: {}", key);
    }
}
