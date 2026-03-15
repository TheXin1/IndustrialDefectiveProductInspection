package com.example.industrialdefectiveproductinspection.service.impl;

import com.example.industrialdefectiveproductinspection.domain.Permission;
import com.example.industrialdefectiveproductinspection.dto.CreatePermissionRequest;
import com.example.industrialdefectiveproductinspection.exception.ServiceException;
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

    @Override
    public Permission createPermission(CreatePermissionRequest request) {
        if (request == null || request.getCode() == null || request.getName() == null || request.getType() == null) {
            throw new ServiceException("非法请求");
        }
        Permission permission = new Permission();
        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setType(request.getType());
        permission.setParentId(request.getParentId());
        permission.setPath(request.getPath());
        permission.setMethod(request.getMethod());
        permission.setDescription(request.getDescription());
        permission.setSortOrder(request.getSortOrder());
        permissionMapper.insert(permission);
        return permission;
    }

    @Override
    public void deletePermission(Long id) {
        permissionMapper.deleteById(id);
    }
}
