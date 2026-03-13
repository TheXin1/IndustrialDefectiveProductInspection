package com.example.industrialdefectiveproductinspection.service.impl;

import com.example.industrialdefectiveproductinspection.dto.DashboardOverview;
import com.example.industrialdefectiveproductinspection.mapper.InspectionRecordMapper;
import com.example.industrialdefectiveproductinspection.service.DashboardService;
import com.example.industrialdefectiveproductinspection.service.InferenceService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {
    private final InspectionRecordMapper inspectionRecordMapper;
    private final InferenceService inferenceService;

    public DashboardServiceImpl(InspectionRecordMapper inspectionRecordMapper,
                                InferenceService inferenceService) {
        this.inspectionRecordMapper = inspectionRecordMapper;
        this.inferenceService = inferenceService;
    }

    @Override
    public DashboardOverview getOverview() {
        Integer todayTotal = safeInt(inspectionRecordMapper.countTodayTotal());
        Integer todayAnomaly = safeInt(inspectionRecordMapper.countTodayAnomaly());
        Integer alertCount = safeInt(inspectionRecordMapper.countAnomalySinceMinutes(60));
        double rate = todayTotal == 0 ? 0D : (double) todayAnomaly / (double) todayTotal;

        String inferenceStatus = resolveInferenceStatus();
        String modelVersion = resolveModelVersion();
        String nodeStatus = "运行中".equals(inferenceStatus) ? "1 / 1 在线" : "0 / 1 在线";

        DashboardOverview overview = new DashboardOverview();
        overview.setTodayCount(todayTotal);
        overview.setDefectRate(rate);
        overview.setAlertCount(alertCount);
        overview.setModelVersion(modelVersion);
        overview.setInferenceStatus(inferenceStatus);
        overview.setNodeStatus(nodeStatus);
        overview.setAlertPolicy("启用");
        overview.setDataSync("正常");
        return overview;
    }

    private Integer safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String resolveInferenceStatus() {
        try {
            Map<String, Object> health = inferenceService.health();
            Object status = health == null ? null : health.get("status");
            if (status != null && "ok".equalsIgnoreCase(String.valueOf(status))) {
                return "运行中";
            }
            return "异常";
        } catch (Exception ex) {
            return "异常";
        }
    }

    private String resolveModelVersion() {
        try {
            Map<String, Object> info = inferenceService.info();
            if (info == null || info.isEmpty()) {
                return "未知";
            }
            Object version = info.get("anomalygpt_ckpt");
            if (version == null) {
                version = info.get("vicuna_ckpt");
            }
            if (version == null) {
                version = info.get("imagebind_ckpt");
            }
            return version == null ? "未知" : String.valueOf(version);
        } catch (Exception ex) {
            return "未知";
        }
    }
}
