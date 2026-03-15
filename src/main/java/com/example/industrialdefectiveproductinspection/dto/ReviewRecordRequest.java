package com.example.industrialdefectiveproductinspection.dto;

import lombok.Data;

@Data
public class ReviewRecordRequest {
    private Integer reviewResult;
    private String reviewNote;
    private Long reviewerId;
}
