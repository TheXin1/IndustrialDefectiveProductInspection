package com.example.industrialdefectiveproductinspection.service.impl;

import com.example.industrialdefectiveproductinspection.domain.InspectionRecord;
import com.example.industrialdefectiveproductinspection.dto.InferenceDetectResponse;
import com.example.industrialdefectiveproductinspection.mapper.InspectionRecordMapper;
import com.example.industrialdefectiveproductinspection.service.InspectionRecordService;
import org.springframework.stereotype.Service;

@Service
public class InspectionRecordServiceImpl implements InspectionRecordService {

    private final InspectionRecordMapper inspectionRecordMapper;

    public InspectionRecordServiceImpl(InspectionRecordMapper inspectionRecordMapper) {
        this.inspectionRecordMapper = inspectionRecordMapper;
    }

    @Override
    public void recordDetection(Long userId, String sourceType, InferenceDetectResponse response) {
        if (response == null || response.getData() == null) {
            return;
        }
        InspectionRecord record = new InspectionRecord();
        record.setUserId(userId);
        record.setSourceType(sourceType == null ? "upload" : sourceType);
        record.setDescription(response.getData().getDescription());
        record.setHasAnomaly(Boolean.TRUE.equals(response.getData().getHasAnomaly()) ? 1 : 0);
        inspectionRecordMapper.insert(record);
    }
}
