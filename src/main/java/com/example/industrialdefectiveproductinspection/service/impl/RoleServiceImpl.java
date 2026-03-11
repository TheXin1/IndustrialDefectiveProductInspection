package com.example.industrialdefectiveproductinspection.service.impl;

import com.example.industrialdefectiveproductinspection.domain.Role;
import com.example.industrialdefectiveproductinspection.dto.CreateRoleRequest;
import com.example.industrialdefectiveproductinspection.dto.UpdateRoleRequest;
import com.example.industrialdefectiveproductinspection.exception.ServiceException;
import com.example.industrialdefectiveproductinspection.mapper.RoleMapper;
import com.example.industrialdefectiveproductinspection.mapper.RolePermissionMapper;
import com.example.industrialdefectiveproductinspection.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;

    public RoleServiceImpl(RoleMapper roleMapper, RolePermissionMapper rolePermissionMapper) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }

    @Override
    public List<Role> listRoles() {
        return roleMapper.selectAll();
    }

    @Override
    public Role getRole(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new ServiceException("role_not_found");
        }
        return role;
    }

    @Override
    @Transactional
    public Role createRole(CreateRoleRequest request) {
        if (request == null || request.getCode() == null || request.getName() == null) {
            throw new ServiceException("role_code_name_required");
        }
        if (roleMapper.selectByCode(request.getCode()) != null) {
            throw new ServiceException("role_code_exists");
        }
        Role role = new Role();
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setBuiltIn(request.getBuiltIn() == null ? 0 : request.getBuiltIn());
        roleMapper.insert(role);
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            rolePermissionMapper.insertBatch(role.getId(), request.getPermissionIds());
        }
        return roleMapper.selectById(role.getId());
    }

    @Override
    @Transactional
    public Role updateRole(Long id, UpdateRoleRequest request) {
        Role existing = roleMapper.selectById(id);
        if (existing == null) {
            throw new ServiceException("role_not_found");
        }
        if (request == null) {
            throw new ServiceException("invalid_request");
        }
        Role toUpdate = new Role();
        toUpdate.setId(id);
        toUpdate.setName(request.getName());
        toUpdate.setDescription(request.getDescription());
        boolean hasField = request.getName() != null || request.getDescription() != null;
        if (hasField) {
            roleMapper.update(toUpdate);
        }
        if (request.getPermissionIds() != null) {
            assignPermissions(id, request.getPermissionIds());
        }
        return roleMapper.selectById(id);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role existing = roleMapper.selectById(id);
        if (existing == null) {
            throw new ServiceException("role_not_found");
        }
        if (existing.getBuiltIn() != null && existing.getBuiltIn() == 1) {
            throw new ServiceException("built_in_role_cannot_delete");
        }
        rolePermissionMapper.deleteByRoleId(id);
        roleMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.deleteByRoleId(roleId);
        if (permissionIds != null && !permissionIds.isEmpty()) {
            rolePermissionMapper.insertBatch(roleId, permissionIds);
        }
    }

    @Override
    public List<Long> getPermissionIds(Long roleId) {
        List<Long> ids = rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
        return ids == null ? Collections.emptyList() : ids;
    }
}
