package com.example.industrialdefectiveproductinspection.service.impl;

import com.example.industrialdefectiveproductinspection.domain.Role;
import com.example.industrialdefectiveproductinspection.domain.User;
import com.example.industrialdefectiveproductinspection.dto.CreateUserRequest;
import com.example.industrialdefectiveproductinspection.dto.UpdateUserRequest;
import com.example.industrialdefectiveproductinspection.exception.ServiceException;
import com.example.industrialdefectiveproductinspection.mapper.UserMapper;
import com.example.industrialdefectiveproductinspection.mapper.UserRoleMapper;
import com.example.industrialdefectiveproductinspection.service.UserService;
import com.example.industrialdefectiveproductinspection.util.PasswordUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;

    public UserServiceImpl(UserMapper userMapper, UserRoleMapper userRoleMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public List<User> listUsers(Integer status) {
        return userMapper.selectAll(status);
    }

    @Override
    public User getUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ServiceException("user_not_found");
        }
        return user;
    }

    @Override
    @Transactional
    public User createUser(CreateUserRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            throw new ServiceException("username_password_required");
        }
        if (userMapper.selectByUsername(request.getUsername()) != null) {
            throw new ServiceException("username_exists");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(PasswordUtils.hash(request.getPassword()));
        user.setDisplayName(request.getDisplayName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        userMapper.insert(user);
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            userRoleMapper.insertBatch(user.getId(), request.getRoleIds());
        }
        return userMapper.selectById(user.getId());
    }

    @Override
    @Transactional
    public User updateUser(Long id, UpdateUserRequest request) {
        User existing = userMapper.selectById(id);
        if (existing == null) {
            throw new ServiceException("user_not_found");
        }
        if (request == null) {
            throw new ServiceException("invalid_request");
        }
        User toUpdate = new User();
        toUpdate.setId(id);
        toUpdate.setDisplayName(request.getDisplayName());
        toUpdate.setEmail(request.getEmail());
        toUpdate.setPhone(request.getPhone());
        toUpdate.setStatus(request.getStatus());
        boolean hasField = request.getDisplayName() != null
                || request.getEmail() != null
                || request.getPhone() != null
                || request.getStatus() != null;
        if (hasField) {
            userMapper.update(toUpdate);
        }
        if (request.getRoleIds() != null) {
            assignRoles(id, request.getRoleIds());
        }
        return userMapper.selectById(id);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRoleMapper.deleteByUserId(id);
        userMapper.deleteById(id);
    }

    @Override
    public List<Role> getRoles(Long userId) {
        return userMapper.selectRolesByUserId(userId);
    }

    @Override
    public List<Long> getRoleIds(Long userId) {
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        return roleIds == null ? Collections.emptyList() : roleIds;
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.deleteByUserId(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            userRoleMapper.insertBatch(userId, roleIds);
        }
    }
}
