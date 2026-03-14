package com.example.industrialdefectiveproductinspection.dto;

import lombok.Data;

@Data
public class InspectionRecordResponse {
    private Long id;
    private Long userId;
    private String sourceType;
    private String imageUrl;
    private String localizationImageUrl;
    private String description;
    private Boolean hasAnomaly;
    private String modelVersion;
    private String createdAt;
}
