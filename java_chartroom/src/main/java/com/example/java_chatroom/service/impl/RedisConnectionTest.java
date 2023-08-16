package com.example.java_chatroom.service.impl;

import redis.clients.jedis.Jedis;

public class RedisConnectionTest {
    public static void main(String[] args) {
        try (Jedis jedis = new Jedis("43.139.243.98", 6379)) {
            // 如果连接成功，会输出 "PONG"，否则会抛出异常
            String pong = jedis.ping();
            System.out.println("Connection to Redis server successful: " + pong);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
