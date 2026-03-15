package com.example.industrialdefectiveproductinspection.dto;

import lombok.Data;

@Data
public class CreatePermissionRequest {
    private String code;
    private String name;
    private String type;
    private Long parentId;
    private String path;
    private String method;
    private String description;
    private Integer sortOrder;
}
