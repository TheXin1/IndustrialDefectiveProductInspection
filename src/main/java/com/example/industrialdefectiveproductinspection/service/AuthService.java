package com.example.industrialdefectiveproductinspection.service;

import com.example.industrialdefectiveproductinspection.dto.LoginRequest;
import com.example.industrialdefectiveproductinspection.dto.LoginResponse;
import com.example.industrialdefectiveproductinspection.dto.RegisterRequest;
import com.example.industrialdefectiveproductinspection.domain.User;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    User register(RegisterRequest request);
}
