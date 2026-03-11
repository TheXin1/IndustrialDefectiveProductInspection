package com.example.industrialdefectiveproductinspection.mapper;

import com.example.industrialdefectiveproductinspection.domain.AuditLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AuditLogMapper {
    int insert(AuditLog log);

    List<AuditLog> selectByUserId(@Param("userId") Long userId);
}
