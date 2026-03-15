package com.example.industrialdefectiveproductinspection.service.impl;

import com.example.industrialdefectiveproductinspection.dto.InferenceDetectResponse;
import com.example.industrialdefectiveproductinspection.exception.ServiceException;
import com.example.industrialdefectiveproductinspection.service.InferenceService;
import com.example.industrialdefectiveproductinspection.util.Utils;
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
import java.util.regex.Pattern;

@Service
public class InferenceServiceImpl implements InferenceService {

    private static final Pattern[] NEGATIVE_PATTERNS = new Pattern[]{
            Pattern.compile("无(明显)?(异常|缺陷|瑕疵|不良)"),
            Pattern.compile("没有(明显)?(异常|缺陷|瑕疵|不良)"),
            Pattern.compile("没有任何(异常|缺陷|瑕疵|不良)"),
            Pattern.compile("未(见|发现)(明显)?(异常|缺陷|瑕疵|不良)"),
            Pattern.compile("未发现任何(异常|缺陷|瑕疵|不良)"),
            Pattern.compile("不存在(异常|缺陷|瑕疵|不良)"),
            Pattern.compile("(正常|良品|无异常)"),
            Pattern.compile("no\\s+(anomaly|defect|abnormality)"),
            Pattern.compile("\\bnormal\\b")
    };
    private static final Pattern[] POSITIVE_PATTERNS = new Pattern[]{
            Pattern.compile("异常"),
            Pattern.compile("缺陷"),
            Pattern.compile("瑕疵"),
            Pattern.compile("不良"),
            Pattern.compile("anomaly"),
            Pattern.compile("defect"),
            Pattern.compile("abnormal")
    };

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
            throw new ServiceException("未检测到图像");
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
                throw new ServiceException("无响应数据");
            }
            boolean hasAnomaly = normalizeAnomalyFlag(
                    response.getBody().getData().getDescription(),
                    response.getBody().getData().getHasAnomaly()
            );
            response.getBody().getData().setHasAnomaly(hasAnomaly);
            return response.getBody();
        } catch (Exception ex) {
            throw new ServiceException("推理服务错误");
        }

    }

    private Map<String, Object> getForObject(String path) {
        try {
            return restTemplate.getForObject(url(path), Map.class);
        } catch (Exception ex) {
            throw new ServiceException("服务不可用");
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
            throw new ServiceException("图片读取失败");
        }
    }



    private boolean normalizeAnomalyFlag(String description, Boolean fallback) {
        String text = description == null ? "" : description.toLowerCase();
        if (text.trim().isEmpty()) {
            return Boolean.TRUE.equals(fallback);
        }

        for (Pattern pattern : NEGATIVE_PATTERNS) {
            if (pattern.matcher(text).find()) {
                return false;
            }
        }

        for (Pattern pattern : POSITIVE_PATTERNS) {
            if (pattern.matcher(text).find()) {
                return true;
            }
        }

        return Boolean.TRUE.equals(fallback);
    }
}
