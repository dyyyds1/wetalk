package com.example.java_chatroom.config;

import com.example.java_chatroom.controller.WebSocketAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private WebSocketAPI webSocketAPI;


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        //通过这个方法，把刚才创建好的handler类注册到具体的路径上
        registry.addHandler(webSocketAPI,"/WebSocketMessage")
                // 通过注册这个特定的HttpSession 拦截器，就可以把用户给 HttpSession 中添加的 Attribute键值对
                // 往我们的 WebSocketSession 里也添加一份
                .addInterceptors(new HttpSessionHandshakeInterceptor());

    }
}
