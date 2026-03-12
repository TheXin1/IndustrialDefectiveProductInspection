package com.example.industrialdefectiveproductinspection.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Role {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer builtIn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
