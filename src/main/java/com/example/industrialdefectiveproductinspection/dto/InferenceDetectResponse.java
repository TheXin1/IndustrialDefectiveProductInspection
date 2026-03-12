package com.example.industrialdefectiveproductinspection.dto;

import lombok.Data;

@Data
public class InferenceDetectResponse {
    private boolean success;
    private InferenceDetectData data;
}
