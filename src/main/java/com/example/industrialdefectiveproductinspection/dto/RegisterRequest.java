package com.example.industrialdefectiveproductinspection.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String displayName;
    private String email;
    private String phone;
}