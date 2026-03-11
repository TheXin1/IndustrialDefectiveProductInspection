package com.example.industrialdefectiveproductinspection.service.impl;

import com.example.industrialdefectiveproductinspection.domain.Permission;
import com.example.industrialdefectiveproductinspection.mapper.PermissionMapper;
import com.example.industrialdefectiveproductinspection.service.PermissionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionServiceImpl implements PermissionService {
    private final PermissionMapper permissionMapper;

    public PermissionServiceImpl(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    @Override
    public List<Permission> listPermissions() {
        return permissionMapper.selectAll();
    }
}
