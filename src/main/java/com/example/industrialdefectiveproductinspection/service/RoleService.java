package com.example.industrialdefectiveproductinspection.service;

import com.example.industrialdefectiveproductinspection.domain.Role;
import com.example.industrialdefectiveproductinspection.dto.CreateRoleRequest;
import com.example.industrialdefectiveproductinspection.dto.UpdateRoleRequest;

import java.util.List;

public interface RoleService {
    List<Role> listRoles();

    Role getRole(Long id);

    Role createRole(CreateRoleRequest request);

    Role updateRole(Long id, UpdateRoleRequest request);

    void deleteRole(Long id);

    void assignPermissions(Long roleId, List<Long> permissionIds);

    List<Long> getPermissionIds(Long roleId);
}
