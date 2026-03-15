package com.example.industrialdefectiveproductinspection.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRequest {
    private String displayName;
    private String email;
    private String phone;
    private String password;
    private Integer status;
    private List<Long> roleIds;
}
