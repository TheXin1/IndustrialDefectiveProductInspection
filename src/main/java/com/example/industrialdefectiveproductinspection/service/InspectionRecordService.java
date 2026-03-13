package com.example.industrialdefectiveproductinspection.service;

import com.example.industrialdefectiveproductinspection.dto.InferenceDetectResponse;

public interface InspectionRecordService {
    void recordDetection(Long userId, String sourceType, InferenceDetectResponse response);
}
