package com.example.industrialdefectiveproductinspection.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatSessionRequest {
    private Long userId;
    private String title;
    private String prompt;
    private Integer maxTgtLen;
    private Double topP;
    private Double temperature;
    private String imageDataUrl;
    private String normalDataUrl;
    private Object result;
    private List<ChatMessageDto> messages;
}