package com.example.industrialdefectiveproductinspection.service.impl;

import com.example.industrialdefectiveproductinspection.domain.Permission;
import com.example.industrialdefectiveproductinspection.domain.Role;
import com.example.industrialdefectiveproductinspection.domain.User;
import com.example.industrialdefectiveproductinspection.dto.LoginRequest;
import com.example.industrialdefectiveproductinspection.dto.LoginResponse;
import com.example.industrialdefectiveproductinspection.exception.ServiceException;
import com.example.industrialdefectiveproductinspection.mapper.UserMapper;
import com.example.industrialdefectiveproductinspection.service.AuthService;
import com.example.industrialdefectiveproductinspection.util.PasswordUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;

    public AuthServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            throw new ServiceException("username_password_required");
        }
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new ServiceException("invalid_credentials");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new ServiceException("user_disabled");
        }
        if (!PasswordUtils.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ServiceException("invalid_credentials");
        }
        userMapper.updateLastLogin(user.getId(), LocalDateTime.now());
        List<Role> roles = userMapper.selectRolesByUserId(user.getId());
        List<Permission> permissions = userMapper.selectPermissionsByUserId(user.getId());

        LoginResponse response = new LoginResponse();
        response.setUser(user);
        response.setRoles(roles);
        response.setPermissions(permissions);
        return response;
    }
}
