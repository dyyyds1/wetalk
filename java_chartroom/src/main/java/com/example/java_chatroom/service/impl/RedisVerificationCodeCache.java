package com.example.java_chatroom.service.impl;

import com.example.java_chatroom.service.VerificationCodeCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class RedisVerificationCodeCache implements VerificationCodeCache {
    private final JedisPool jedisPool;

    @Autowired
    public RedisVerificationCodeCache(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public void saveCode(String email, String code, long expirationInSeconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            // 使用邮箱地址作为 Redis 键，验证码作为 Redis 值，设置过期时间
            jedis.setex(email, expirationInSeconds, code);
        }catch (Exception e) {
            // 处理异常，例如记录日志或抛出自定义异常
            e.printStackTrace();
        }
    }

    @Override
    public boolean isCodeValid(String email, String code) {
        try (Jedis jedis = jedisPool.getResource()) {
            // 从 Redis 中获取保存的验证码
            String savedCode = jedis.get(email);
            if (savedCode == null) {
                // 验证码不存在，已过期或者未生成
                return false;
            } else if (!savedCode.equals(code)) {
                // 验证码不匹配
                return false;
            } else {
                // 验证码匹配，检查是否过期
                Long ttl = jedis.ttl(email);
                if (ttl != null && ttl < 0) {
                    // 验证码已过期
                    return false;
                }
                // 验证码有效
                return true;
            }
        }
    }
}
