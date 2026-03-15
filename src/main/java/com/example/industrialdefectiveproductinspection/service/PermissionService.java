package com.example.industrialdefectiveproductinspection.service;

import com.example.industrialdefectiveproductinspection.domain.Permission;
import com.example.industrialdefectiveproductinspection.dto.CreatePermissionRequest;

import java.util.List;

public interface PermissionService {
    List<Permission> listPermissions();

    Permission createPermission(CreatePermissionRequest request);

    void deletePermission(Long id);
}
