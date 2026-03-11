package com.example.industrialdefectiveproductinspection.service.impl;

import com.example.industrialdefectiveproductinspection.domain.AuditLog;
import com.example.industrialdefectiveproductinspection.mapper.AuditLogMapper;
import com.example.industrialdefectiveproductinspection.service.AuditLogService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogMapper auditLogMapper;

    public AuditLogServiceImpl(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    @Override
    public void record(AuditLog log) {
        if (log != null) {
            auditLogMapper.insert(log);
        }
    }

    @Override
    public List<AuditLog> listByUserId(Long userId) {
        return auditLogMapper.selectByUserId(userId);
    }
}
