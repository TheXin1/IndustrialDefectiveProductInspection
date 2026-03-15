package com.example.industrialdefectiveproductinspection.controller;

import com.example.industrialdefectiveproductinspection.dto.ApiResponse;
import com.example.industrialdefectiveproductinspection.dto.InferenceDetectResponse;
import com.example.industrialdefectiveproductinspection.service.InferenceService;
import com.example.industrialdefectiveproductinspection.service.InspectionRecordService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
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
        String imageDataUrl = buildImageDataUrl(image);
        String localizationDataUrl = buildHeatmapDataUrl(response);
        inspectionRecordService.recordDetection(userId, "upload", response, imageDataUrl, localizationDataUrl);
        return ApiResponse.ok(response);
    }

    @PostMapping(value = "/chat", consumes = "multipart/form-data")
    public ApiResponse<InferenceDetectResponse> chat(
            @RequestPart("image") MultipartFile image,
            @RequestPart(value = "normal_image", required = false) MultipartFile normalImage,
            @RequestParam(value = "prompt", required = false) String prompt,
            @RequestParam(value = "max_tgt_len", required = false) Integer maxTgtLen,
            @RequestParam(value = "top_p", required = false) Double topP,
            @RequestParam(value = "temperature", required = false) Double temperature,
            @RequestParam(value = "userId", required = false) Long userId) {
        InferenceDetectResponse response = inferenceService.detect(image, normalImage, prompt, maxTgtLen, topP, temperature);
        return ApiResponse.ok(response);
    }

    private String buildImageDataUrl(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return null;
        }
        try {
            String contentType = image.getContentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = "image/png";
            }
            String base64 = Base64.getEncoder().encodeToString(image.getBytes());
            return "data:" + contentType + ";base64," + base64;
        } catch (Exception ex) {
            return null;
        }
    }

    private String buildHeatmapDataUrl(InferenceDetectResponse response) {
        if (response == null || response.getData() == null) {
            return null;
        }
        String base64 = response.getData().getLocalizationImageBase64();
        if (base64 == null || base64.isBlank()) {
            return null;
        }
        return "data:image/png;base64," + base64;
    }
}
