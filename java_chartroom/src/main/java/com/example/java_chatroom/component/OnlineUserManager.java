package com.example.java_chatroom.component;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

//通过这个类来记录当前用户登录状态(维护 userId 和 WebSocketSession之间的映射)
@Component
public class OnlineUserManager {
    //由于多线程，应当考虑线程安全
    private ConcurrentHashMap<Integer, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // 1) 用户上线，插入键值对
    public void online(int userId, WebSocketSession session) {
        // 检查是否已经存在该用户的 WebSocketSession
        WebSocketSession existingSession = sessions.get(userId);
        if (existingSession != null) {
            try {
                // 关闭前一个登录的 WebSocketSession
                existingSession.close();
            } catch (Exception e) {
                // 处理关闭异常
                e.printStackTrace();
            }
        }

        // 添加新的 WebSocketSession
        sessions.put(userId, session);
        System.out.println("[" + userId + "]" + "上线");
    }

    // 2) 用户下线，删除键值对
    public void offline(int userId,WebSocketSession session){
        WebSocketSession existingSession = sessions.get(userId);
        if (existingSession == session) {
            sessions.remove((userId));
            System.out.println("[" + userId + "]" + "下线");

        }
    }
    // 3) 根据 userId 获取到 WebSocketSession
    public WebSocketSession getSession(int userId){
        return sessions.get(userId);
    }
}
