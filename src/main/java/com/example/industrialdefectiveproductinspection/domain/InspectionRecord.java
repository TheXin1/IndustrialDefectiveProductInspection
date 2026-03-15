package com.example.industrialdefectiveproductinspection.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InspectionRecord {
    private Long id;
    private Long userId;
    private String sourceType;
    private String imageUrl;
    private String localizationImageUrl;
    private String description;
    private Integer hasAnomaly;
    private String modelVersion;
    private Integer reviewStatus;
    private Integer reviewResult;
    private String reviewNote;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
