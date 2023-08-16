package com.example.java_chatroom.config;

import com.example.java_chatroom.service.impl.RedisVerificationCodeCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;

@Configuration
public class AppConfig {
    
    // 在这里创建 JedisPool 的 bean
    @Bean
    public JedisPool jedisPool() {
        return new JedisPool("43.139.243.98", 6379); // 你的 Redis 服务器地址和端口号
    }
    @Bean
    public RedisVerificationCodeCache verificationCodeCache(JedisPool jedisPool) {
        return new RedisVerificationCodeCache(jedisPool);
    }

}