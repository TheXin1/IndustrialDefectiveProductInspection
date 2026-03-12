package com.example.industrialdefectiveproductinspection.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InferenceDetectData {
    @JsonProperty("description")
    private String description;

    @JsonProperty("localization_image_base64")
    private String localizationImageBase64;

    @JsonProperty("has_anomaly")
    private Boolean hasAnomaly;
}
