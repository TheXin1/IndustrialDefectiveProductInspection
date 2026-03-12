package com.example.industrialdefectiveproductinspection.mapper;

import com.example.industrialdefectiveproductinspection.domain.Permission;
import com.example.industrialdefectiveproductinspection.domain.Role;
import com.example.industrialdefectiveproductinspection.domain.User;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserMapper {
    List<User> selectAll(@Param("status") Integer status);

    User selectById(@Param("id") Long id);

    User selectByUsername(@Param("username") String username);

    int insert(User user);

    int update(User user);

    int deleteById(@Param("id") Long id);

    int updateLastLogin(@Param("id") Long id, @Param("lastLoginAt") LocalDateTime lastLoginAt);

    List<Role> selectRolesByUserId(@Param("userId") Long userId);

    List<Permission> selectPermissionsByUserId(@Param("userId") Long userId);
}
