package com.example.industrialdefectiveproductinspection.service.impl;

import com.example.industrialdefectiveproductinspection.dto.InferenceDetectResponse;
import com.example.industrialdefectiveproductinspection.exception.ServiceException;
import com.example.industrialdefectiveproductinspection.service.InferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class InferenceServiceImpl implements InferenceService {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public InferenceServiceImpl(@Value("${inference.api.base-url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    @Override
    public Map<String, Object> health() {
        return getForObject("/api/v1/health");
    }

    @Override
    public Map<String, Object> info() {
        return getForObject("/api/v1/info");
    }

    @Override
    public Map<String, Object> startupStatus() {
        return getForObject("/api/v1/startup_status");
    }

    @Override
    public InferenceDetectResponse detect(MultipartFile image,
                                          MultipartFile normalImage,
                                          String prompt,
                                          Integer maxTgtLen,
                                          Double topP,
                                          Double temperature) {
        if (image == null || image.isEmpty()) {
            throw new ServiceException("image_required");
        }
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", toResource(image));
        if (normalImage != null && !normalImage.isEmpty()) {
            body.add("normal_image", toResource(normalImage));
        }
        if (prompt != null) {
            body.add("prompt", prompt);
        }
        if (maxTgtLen != null) {
            body.add("max_tgt_len", String.valueOf(maxTgtLen));
        }
        if (topP != null) {
            body.add("top_p", String.valueOf(topP));
        }
        if (temperature != null) {
            body.add("temperature", String.valueOf(temperature));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<InferenceDetectResponse> response = restTemplate.postForEntity(
                    url("/api/v1/detect"), entity, InferenceDetectResponse.class);
            if (response.getBody() == null) {
                throw new ServiceException("inference_empty_response");
            }
            return response.getBody();
        } catch (Exception ex) {
            throw new ServiceException("inference_service_unavailable");
        }
    }

    private Map<String, Object> getForObject(String path) {
        try {
            return restTemplate.getForObject(url(path), Map.class);
        } catch (Exception ex) {
            throw new ServiceException("inference_service_unavailable");
        }
    }

    private String url(String path) {
        if (baseUrl.endsWith("/") && path.startsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + path;
        }
        if (!baseUrl.endsWith("/") && !path.startsWith("/")) {
            return baseUrl + "/" + path;
        }
        return baseUrl + path;
    }

    private ByteArrayResource toResource(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            return new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
                }
            };
        } catch (IOException ex) {
            throw new ServiceException("file_read_failed");
        }
    }
}
