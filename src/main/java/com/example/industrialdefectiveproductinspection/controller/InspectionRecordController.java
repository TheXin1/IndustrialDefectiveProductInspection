package com.example.industrialdefectiveproductinspection.controller;

import com.example.industrialdefectiveproductinspection.dto.ApiResponse;
import com.example.industrialdefectiveproductinspection.dto.InspectionRecordResponse;
import com.example.industrialdefectiveproductinspection.dto.PageResult;
import com.example.industrialdefectiveproductinspection.service.InspectionRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/records")
public class InspectionRecordController {
    private final InspectionRecordService inspectionRecordService;

    public InspectionRecordController(InspectionRecordService inspectionRecordService) {
        this.inspectionRecordService = inspectionRecordService;
    }

    @GetMapping
    public ApiResponse<PageResult<InspectionRecordResponse>> list(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "hasAnomaly", required = false) Integer hasAnomaly,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "startAt", required = false) String startAt,
            @RequestParam(value = "endAt", required = false) String endAt,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return ApiResponse.ok(inspectionRecordService.list(userId, hasAnomaly, keyword, startAt, endAt, page, size));
    }
}
