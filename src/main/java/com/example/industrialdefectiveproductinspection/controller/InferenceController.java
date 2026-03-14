package com.example.industrialdefectiveproductinspection.controller;

import com.example.industrialdefectiveproductinspection.dto.ApiResponse;
import com.example.industrialdefectiveproductinspection.dto.InferenceDetectResponse;
import com.example.industrialdefectiveproductinspection.service.InferenceService;
import com.example.industrialdefectiveproductinspection.service.InspectionRecordService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/inference")
public class InferenceController {

    private final InferenceService inferenceService;
    private final InspectionRecordService inspectionRecordService;

    public InferenceController(InferenceService inferenceService,
                               InspectionRecordService inspectionRecordService) {
        this.inferenceService = inferenceService;
        this.inspectionRecordService = inspectionRecordService;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.ok(inferenceService.health());
    }

    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> info() {
        return ApiResponse.ok(inferenceService.info());
    }

    @GetMapping("/startup_status")
    public ApiResponse<Map<String, Object>> startupStatus() {
        return ApiResponse.ok(inferenceService.startupStatus());
    }

    @PostMapping(value = "/detect", consumes = "multipart/form-data")
    public ApiResponse<InferenceDetectResponse> detect(
            @RequestPart("image") MultipartFile image,
            @RequestPart(value = "normal_image", required = false) MultipartFile normalImage,
            @RequestParam(value = "prompt", required = false) String prompt,
            @RequestParam(value = "max_tgt_len", required = false) Integer maxTgtLen,
            @RequestParam(value = "top_p", required = false) Double topP,
            @RequestParam(value = "temperature", required = false) Double temperature,
            @RequestParam(value = "userId", required = false) Long userId) {
        InferenceDetectResponse response = inferenceService.detect(image, normalImage, prompt, maxTgtLen, topP, temperature);
        inspectionRecordService.recordDetection(userId, "upload", response);
        return ApiResponse.ok(response);
    }
}
