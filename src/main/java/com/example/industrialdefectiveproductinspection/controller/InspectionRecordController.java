package com.example.industrialdefectiveproductinspection.controller;

import com.example.industrialdefectiveproductinspection.dto.ApiResponse;
import com.example.industrialdefectiveproductinspection.dto.InspectionRecordResponse;
import com.example.industrialdefectiveproductinspection.dto.PageResult;
import com.example.industrialdefectiveproductinspection.dto.ReviewRecordRequest;
import com.example.industrialdefectiveproductinspection.service.InspectionRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

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
            @RequestParam(value = "reviewStatus", required = false) Integer reviewStatus,
            @RequestParam(value = "reviewResult", required = false) Integer reviewResult,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "startAt", required = false) String startAt,
            @RequestParam(value = "endAt", required = false) String endAt,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return ApiResponse.ok(
                inspectionRecordService.list(userId, hasAnomaly, reviewStatus, reviewResult, keyword, startAt, endAt, page, size)
        );
    }

    @PutMapping("/{id}/review")
    public ApiResponse<Void> review(@PathVariable("id") Long id, @RequestBody ReviewRecordRequest request) {
        inspectionRecordService.reviewRecord(
                id,
                request == null ? null : request.getReviewResult(),
                request == null ? null : request.getReviewNote(),
                request == null ? null : request.getReviewerId()
        );
        return ApiResponse.ok(null);
    }
}
