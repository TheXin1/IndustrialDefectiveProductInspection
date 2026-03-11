package com.example.industrialdefectiveproductinspection.dto;

import com.example.industrialdefectiveproductinspection.domain.Permission;
import com.example.industrialdefectiveproductinspection.domain.Role;
import com.example.industrialdefectiveproductinspection.domain.User;
import lombok.Data;

import java.util.List;

@Data
public class LoginResponse {
    private User user;
    private List<Role> roles;
    private List<Permission> permissions;
}
