package com.example.industrialdefectiveproductinspection.service.impl;

import com.example.industrialdefectiveproductinspection.domain.InspectionRecord;
import com.example.industrialdefectiveproductinspection.dto.InferenceDetectResponse;
import com.example.industrialdefectiveproductinspection.dto.InspectionRecordResponse;
import com.example.industrialdefectiveproductinspection.dto.PageResult;
import com.example.industrialdefectiveproductinspection.mapper.InspectionRecordMapper;
import com.example.industrialdefectiveproductinspection.service.InspectionRecordService;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class InspectionRecordServiceImpl implements InspectionRecordService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

    @Override
    public PageResult<InspectionRecordResponse> list(Long userId, Integer hasAnomaly, String keyword, String startAt, String endAt, int page, int size) {
        int safePage = Math.max(page,1);
        int safeSize = Math.min(Math.max(size,1),100);
        int offset = (safePage - 1) * safeSize;

        Integer total = inspectionRecordMapper.countPage(userId, hasAnomaly, keyword, startAt, endAt);

        List<InspectionRecord> records = inspectionRecordMapper.selectPage(userId, hasAnomaly, keyword, startAt, endAt, page, size);

        List<InspectionRecordResponse> responses = records.stream().map(this::toResponse).collect((Collectors.toList()));

        PageResult<InspectionRecordResponse> result = new PageResult<>();

        result.setPage(safePage);
        result.setSize(safeSize);
        result.setTotal(total == null ? 0 : total);
        result.setRecords(responses);

        return  result;
    }

    private InspectionRecordResponse toResponse(InspectionRecord record) {
        InspectionRecordResponse response = new InspectionRecordResponse();
        response.setId(record.getId());
        response.setUserId(record.getUserId());
        response.setSourceType(record.getSourceType());
        response.setImageUrl(record.getImageUrl());
        response.setLocalizationImageUrl(record.getLocalizationImageUrl());
        response.setDescription(record.getDescription());
        response.setHasAnomaly(record.getHasAnomaly() != null && record.getHasAnomaly() == 1);
        response.setModelVersion(record.getModelVersion());
        response.setCreatedAt(record.getCreatedAt() == null ? null : record.getCreatedAt().format(FORMATTER));
        return response;
    }
}
