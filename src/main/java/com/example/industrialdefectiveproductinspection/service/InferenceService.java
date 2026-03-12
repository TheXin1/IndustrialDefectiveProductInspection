package com.example.industrialdefectiveproductinspection.service;

import com.example.industrialdefectiveproductinspection.dto.InferenceDetectResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface InferenceService {
    Map<String, Object> health();

    Map<String, Object> info();

    Map<String, Object> startupStatus();

    InferenceDetectResponse detect(MultipartFile image,
                                   MultipartFile normalImage,
                                   String prompt,
                                   Integer maxTgtLen,
                                   Double topP,
                                   Double temperature);
}
