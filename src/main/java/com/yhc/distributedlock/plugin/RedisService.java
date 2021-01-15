package com.yhc.distributedlock.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.net.ConnectException;
import java.sql.Time;
import java.util.concurrent.TimeUnit;

/**
 * redis工具类，此处所有有效期均以秒为单位来计算
 */
@Configuration
public class RedisService {

    private static final Logger log = LoggerFactory.getLogger(RedisService.class);

    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * SET操作
     *
     * @param key
     * @param value
     * @return 是否成功
     */
    public boolean set(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("# redis set操作异常：" + e.getMessage());
            return false;
        }
    }

    /**
     * GET操作
     *
     * @param key
     * @return value
     */
    public String get(String key) {
        try {
            return (String) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("# redis get操作异常：" + e.getMessage());
            return null;
        }
    }

    /**
     * DELETE操作
     *
     * @param key
     * @return 是否成功
     */
    public boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("# redis get操作异常：" + e.getMessage());
            return false;
        }
    }

    /**
     * SETNX操作
     *
     * @param key
     * @param value
     * @param expireTime（单位：秒）
     * @return 是否成功
     */
    public boolean setnx(String key, String value, long expireTime) {
        try {
            return redisTemplate.opsForValue().setIfAbsent(key, value, expireTime, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("# redis setnx操作异常：" + e.getMessage());
            return false;
        }

    }

    /**
     * EXPIRE操作
     *
     * @param key
     * @param expireTime
     * @return
     */
    public boolean expire(String key, long expireTime) {
        try {
            return redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("# redis expire操作异常：" + e.getMessage());
            return false;
        }
    }

    /**
     * GETEXPIRE操作
     *
     * @param key
     * @return
     */
    public long getExpire(String key) {
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("# redis ttl操作异常：" + e.getMessage());
            return 0;
        }

    }

}
