package com.example.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value, Long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("SET Error: {}", e.getMessage());
        }
    }

    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("GET Error: {}", e.getMessage());
            return null;
        }
    }

    public boolean exists(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("EXISTS Error: {}", e.getMessage());
            return false;
        }
    }

    public Long increment(String key, long value) {
        try {
            return redisTemplate.opsForValue().increment(key, value);
        } catch (Exception e) {
            log.error("INCR Error: {}", e.getMessage());
            return null;
        }
    }

    public Long increment(String key) {
        return increment(key, 1);
    }

    public Long decrement(String key) {
        try {
            return redisTemplate.opsForValue().decrement(key);
        } catch (Exception e) {
            log.error("DECR Error: {}", e.getMessage());
            return null;
        }
    }

    public void pushToList(String key, String value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
        } catch (Exception e) {
            log.error("LIST PUSH Error: {}", e.getMessage());
        }
    }
}
