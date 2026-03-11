package com.example.industrialdefectiveproductinspection.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Permission {
    private Long id;
    private String code;
    private String name;
    private String type;
    private Long parentId;
    private String path;
    private String method;
    private String description;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
