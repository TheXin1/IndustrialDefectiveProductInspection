package com.example.industrialdefectiveproductinspection.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLog {
    private Long id;
    private Long userId;
    private String action;
    private String resource;
    private String ip;
    private String detail;
    private LocalDateTime createdAt;
}
