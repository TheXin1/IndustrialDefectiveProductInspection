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
    private Integer reviewStatus;
    private Integer reviewResult;
    private String reviewNote;
    private Long reviewedBy;
    private String reviewedAt;
    private String createdAt;
}
