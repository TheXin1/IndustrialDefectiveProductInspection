package com.example.industrialdefectiveproductinspection.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatSession {
    private Long id;
    private Long userId;
    private String title;
    private String prompt;
    private Integer maxTgtLen;
    private Double topP;
    private Double temperature;
    private String imageDataUrl;
    private String normalDataUrl;
    private String resultJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}