package com.example.industrialdefectiveproductinspection.service;

import com.example.industrialdefectiveproductinspection.domain.AuditLog;

import java.util.List;

public interface AuditLogService {
    void record(AuditLog log);

    List<AuditLog> listByUserId(Long userId);
}
