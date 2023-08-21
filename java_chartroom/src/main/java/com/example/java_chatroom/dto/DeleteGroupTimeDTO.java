package com.example.java_chatroom.dto;

import lombok.Data;

import java.util.Date;

@Data
public class DeleteGroupTimeDTO {
    private int sessionId;
    private Date deleteTime;

    private Date startTime=new Date(0);
}
