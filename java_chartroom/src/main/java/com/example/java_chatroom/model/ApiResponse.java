package com.example.java_chatroom.model;

import lombok.Data;

@Data
public class ApiResponse {
    private String status;
    private Object data;

    public ApiResponse(String status,Object data){
        this.data=data;
        this.status=status;
    }


    // 省略 getter 和 setter 方法
}