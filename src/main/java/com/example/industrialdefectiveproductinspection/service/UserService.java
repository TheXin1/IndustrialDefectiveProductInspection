package com.example.industrialdefectiveproductinspection.service;

import com.example.industrialdefectiveproductinspection.domain.Role;
import com.example.industrialdefectiveproductinspection.domain.User;
import com.example.industrialdefectiveproductinspection.dto.CreateUserRequest;
import com.example.industrialdefectiveproductinspection.dto.UpdateUserRequest;

import java.util.List;

public interface UserService {
    List<User> listUsers(Integer status);

    User getUser(Long id);

    User createUser(CreateUserRequest request);

    User updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);

    List<Role> getRoles(Long userId);

    List<Long> getRoleIds(Long userId);

    void assignRoles(Long userId, List<Long> roleIds);
}
