package com.example.industrialdefectiveproductinspection.mapper;

import com.example.industrialdefectiveproductinspection.domain.Permission;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PermissionMapper {
    List<Permission> selectAll();

    List<Permission> selectByIds(@Param("ids") List<Long> ids);
}
