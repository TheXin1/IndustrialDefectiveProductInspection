package com.example.industrialdefectiveproductinspection.mapper;

import com.example.industrialdefectiveproductinspection.domain.Role;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoleMapper {
    List<Role> selectAll();

    Role selectById(@Param("id") Long id);

    Role selectByCode(@Param("code") String code);

    int insert(Role role);

    int update(Role role);

    int deleteById(@Param("id") Long id);
}
