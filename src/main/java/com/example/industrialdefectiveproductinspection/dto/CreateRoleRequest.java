package com.example.industrialdefectiveproductinspection.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateRoleRequest {
    private String code;
    private String name;
    private String description;
    private Integer builtIn;
    private List<Long> permissionIds;
}
