package com.example.industrialdefectiveproductinspection.service.impl;

import com.example.industrialdefectiveproductinspection.domain.Permission;
import com.example.industrialdefectiveproductinspection.domain.Role;
import com.example.industrialdefectiveproductinspection.domain.User;
import com.example.industrialdefectiveproductinspection.dto.LoginRequest;
import com.example.industrialdefectiveproductinspection.dto.LoginResponse;
import com.example.industrialdefectiveproductinspection.dto.RegisterRequest;
import com.example.industrialdefectiveproductinspection.exception.ServiceException;
import com.example.industrialdefectiveproductinspection.mapper.RoleMapper;
import com.example.industrialdefectiveproductinspection.mapper.UserMapper;
import com.example.industrialdefectiveproductinspection.mapper.UserRoleMapper;
import com.example.industrialdefectiveproductinspection.service.AuthService;
import com.example.industrialdefectiveproductinspection.util.PasswordUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    public AuthServiceImpl(UserMapper userMapper,
                           RoleMapper roleMapper,
                           UserRoleMapper userRoleMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            throw new ServiceException("请输入用户名与密码");
        }
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new ServiceException("用户名错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new ServiceException("用户状态异常");
        }
        if (!PasswordUtils.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ServiceException("密码错误");
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

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            throw new ServiceException("请输入用户名与密码");
        }
        if (userMapper.selectByUsername(request.getUsername()) != null) {
            throw new ServiceException("用户已存在");
        }
        Role defaultRole = roleMapper.selectByCode("USER");
        if (defaultRole == null) {
            throw new ServiceException("default_role_missing");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(PasswordUtils.hash(request.getPassword()));
        user.setDisplayName(request.getDisplayName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(2);
        userMapper.insert(user);
        userRoleMapper.insertBatch(user.getId(), List.of(defaultRole.getId()));
        return userMapper.selectById(user.getId());
    }
}
