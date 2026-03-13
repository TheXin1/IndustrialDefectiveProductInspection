package com.example.industrialdefectiveproductinspection.dto;

import lombok.Data;

@Data
public class ChatMessageDto {
    private String role;
    private String content;
    private String time;
}