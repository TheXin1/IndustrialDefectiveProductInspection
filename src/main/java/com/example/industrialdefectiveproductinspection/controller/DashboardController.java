package com.example.industrialdefectiveproductinspection.controller;

import com.example.industrialdefectiveproductinspection.dto.ApiResponse;
import com.example.industrialdefectiveproductinspection.dto.DashboardOverview;
import com.example.industrialdefectiveproductinspection.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    public ApiResponse<DashboardOverview> overview() {
        return ApiResponse.ok(dashboardService.getOverview());
    }
}
