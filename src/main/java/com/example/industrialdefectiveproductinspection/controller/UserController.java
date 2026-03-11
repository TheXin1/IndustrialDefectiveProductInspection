package com.example.industrialdefectiveproductinspection.controller;

import com.example.industrialdefectiveproductinspection.domain.Role;
import com.example.industrialdefectiveproductinspection.domain.User;
import com.example.industrialdefectiveproductinspection.dto.ApiResponse;
import com.example.industrialdefectiveproductinspection.dto.AssignRolesRequest;
import com.example.industrialdefectiveproductinspection.dto.CreateUserRequest;
import com.example.industrialdefectiveproductinspection.dto.UpdateUserRequest;
import com.example.industrialdefectiveproductinspection.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<User>> list(@RequestParam(value = "status", required = false) Integer status) {
        return ApiResponse.ok(userService.listUsers(status));
    }

    @GetMapping("/{id}")
    public ApiResponse<User> get(@PathVariable("id") Long id) {
        return ApiResponse.ok(userService.getUser(id));
    }

    @PostMapping
    public ApiResponse<User> create(@RequestBody CreateUserRequest request) {
        return ApiResponse.ok(userService.createUser(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<User> update(@PathVariable("id") Long id, @RequestBody UpdateUserRequest request) {
        return ApiResponse.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}/roles")
    public ApiResponse<List<Role>> roles(@PathVariable("id") Long id) {
        return ApiResponse.ok(userService.getRoles(id));
    }

    @PostMapping("/{id}/roles")
    public ApiResponse<Void> assignRoles(@PathVariable("id") Long id, @RequestBody AssignRolesRequest request) {
        userService.assignRoles(id, request == null ? null : request.getRoleIds());
        return ApiResponse.ok(null);
    }
}
