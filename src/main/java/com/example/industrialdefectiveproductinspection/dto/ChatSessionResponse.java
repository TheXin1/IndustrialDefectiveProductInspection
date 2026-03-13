package com.example.industrialdefectiveproductinspection.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatSessionResponse {
    private Long id;
    private Long userId;
    private String title;
    private String prompt;
    private Integer maxTgtLen;
    private Double topP;
    private Double temperature;
    private String imageDataUrl;
    private String normalDataUrl;
    private Object result;
    private String createdAt;
    private String updatedAt;
    private List<ChatMessageDto> messages;
}