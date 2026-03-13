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
    private LocalDateTime createdAt;
}
