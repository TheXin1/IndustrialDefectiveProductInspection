package com.example.industrialdefectiveproductinspection.service;

import com.example.industrialdefectiveproductinspection.dto.LoginRequest;
import com.example.industrialdefectiveproductinspection.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
