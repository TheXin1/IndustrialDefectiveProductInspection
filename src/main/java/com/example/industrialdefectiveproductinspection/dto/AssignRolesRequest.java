package com.example.industrialdefectiveproductinspection.dto;

import lombok.Data;

import java.util.List;

@Data
public class AssignRolesRequest {
    private List<Long> roleIds;
}
