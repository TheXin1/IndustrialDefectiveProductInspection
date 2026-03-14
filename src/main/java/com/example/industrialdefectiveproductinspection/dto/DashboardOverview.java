package com.example.industrialdefectiveproductinspection.dto;

import lombok.Data;

@Data
public class DashboardOverview {
    private Integer todayCount;
    private Double defectRate;
    private Integer alertCount;
    private String modelVersion;
    private String inferenceStatus;
    private String nodeStatus;
/*
    private String alertPolicy;
    private String dataSync;
*/
}
