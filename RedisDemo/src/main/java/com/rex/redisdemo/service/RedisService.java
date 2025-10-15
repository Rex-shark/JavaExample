package com.rex.redisdemo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ValueOperations<String, String> valueOps;

    @Autowired
    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOps = redisTemplate.opsForValue();
    }

    public Map<String, String> getAll() {
        Set<String> keys = redisTemplate.keys("*");
        Map<String, String> result = new HashMap<>();
        if (keys != null) {
            for (String key : keys) {
                String value = valueOps.get(key);
                result.put(key, value);
            }
        }
        return result;
    }

    public void set(String key, String value) {
        valueOps.set(key, value);
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(valueOps.get(key));
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}

