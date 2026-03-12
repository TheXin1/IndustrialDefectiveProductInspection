package com.example.industrialdefectiveproductinspection.controller;

import com.example.industrialdefectiveproductinspection.domain.Role;
import com.example.industrialdefectiveproductinspection.dto.ApiResponse;
import com.example.industrialdefectiveproductinspection.dto.AssignPermissionsRequest;
import com.example.industrialdefectiveproductinspection.dto.CreateRoleRequest;
import com.example.industrialdefectiveproductinspection.dto.UpdateRoleRequest;
import com.example.industrialdefectiveproductinspection.service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ApiResponse<List<Role>> list() {
        return ApiResponse.ok(roleService.listRoles());
    }

    @GetMapping("/{id}")
    public ApiResponse<Role> get(@PathVariable("id") Long id) {
        return ApiResponse.ok(roleService.getRole(id));
    }

    @PostMapping
    public ApiResponse<Role> create(@RequestBody CreateRoleRequest request) {
        return ApiResponse.ok(roleService.createRole(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Role> update(@PathVariable("id") Long id, @RequestBody UpdateRoleRequest request) {
        return ApiResponse.ok(roleService.updateRole(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        roleService.deleteRole(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}/permissions")
    public ApiResponse<List<Long>> permissionIds(@PathVariable("id") Long id) {
        return ApiResponse.ok(roleService.getPermissionIds(id));
    }

    @PostMapping("/{id}/permissions")
    public ApiResponse<Void> assignPermissions(@PathVariable("id") Long id, @RequestBody AssignPermissionsRequest request) {
        roleService.assignPermissions(id, request == null ? null : request.getPermissionIds());
        return ApiResponse.ok(null);
    }
}
