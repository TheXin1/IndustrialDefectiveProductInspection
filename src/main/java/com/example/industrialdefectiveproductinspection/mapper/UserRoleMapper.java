package com.example.industrialdefectiveproductinspection.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserRoleMapper {
    int deleteByUserId(@Param("userId") Long userId);

    int insertBatch(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
}
