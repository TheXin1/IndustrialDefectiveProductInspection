package com.example.industrialdefectiveproductinspection.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private String displayName;
    private String email;
    private String phone;
    private Integer status;
    private List<Long> roleIds;
}
