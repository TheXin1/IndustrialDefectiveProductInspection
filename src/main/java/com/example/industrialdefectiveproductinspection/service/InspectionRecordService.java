package com.example.industrialdefectiveproductinspection.service;

import com.example.industrialdefectiveproductinspection.dto.InferenceDetectResponse;
import com.example.industrialdefectiveproductinspection.dto.InspectionRecordResponse;
import com.example.industrialdefectiveproductinspection.dto.PageResult;

public interface InspectionRecordService {
    void recordDetection(Long userId, String sourceType, InferenceDetectResponse response);

    PageResult<InspectionRecordResponse> list(Long userId,
                                              Integer hasAnomaly,
                                              String keyword,
                                              String startAt,
                                              String endAt,
                                              int page,
                                              int size
                                              );
}
