package com.example.industrialdefectiveproductinspection.controller;

import com.example.industrialdefectiveproductinspection.domain.Permission;
import com.example.industrialdefectiveproductinspection.dto.ApiResponse;
import com.example.industrialdefectiveproductinspection.service.PermissionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public ApiResponse<List<Permission>> list() {
        return ApiResponse.ok(permissionService.listPermissions());
    }
}
