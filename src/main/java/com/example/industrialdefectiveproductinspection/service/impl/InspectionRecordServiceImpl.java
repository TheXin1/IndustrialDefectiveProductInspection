package com.example.industrialdefectiveproductinspection.service.impl;

import com.example.industrialdefectiveproductinspection.domain.InspectionRecord;
import com.example.industrialdefectiveproductinspection.dto.InferenceDetectResponse;
import com.example.industrialdefectiveproductinspection.dto.InspectionRecordResponse;
import com.example.industrialdefectiveproductinspection.dto.PageResult;
import com.example.industrialdefectiveproductinspection.mapper.InspectionRecordMapper;
import com.example.industrialdefectiveproductinspection.service.InspectionRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
public class InspectionRecordServiceImpl implements InspectionRecordService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern[] NEGATIVE_PATTERNS = new Pattern[]{
            Pattern.compile("无(明显)?(异常|缺陷|瑕疵|不良)"),
            Pattern.compile("没有(明显)?(异常|缺陷|瑕疵|不良)"),
            Pattern.compile("没有任何(异常|缺陷|瑕疵|不良)"),
            Pattern.compile("未(见|发现)(明显)?(异常|缺陷|瑕疵|不良)"),
            Pattern.compile("未发现任何(异常|缺陷|瑕疵|不良)"),
            Pattern.compile("不存在(异常|缺陷|瑕疵|不良)"),
            Pattern.compile("(正常|良品|无异常)"),
            Pattern.compile("no\\s+(anomaly|defect|abnormality)"),
            Pattern.compile("\\bnormal\\b")
    };
    private static final Pattern[] POSITIVE_PATTERNS = new Pattern[]{
            Pattern.compile("异常"),
            Pattern.compile("缺陷"),
            Pattern.compile("瑕疵"),
            Pattern.compile("不良"),
            Pattern.compile("anomaly"),
            Pattern.compile("defect"),
            Pattern.compile("abnormal")
    };


    private final InspectionRecordMapper inspectionRecordMapper;

    public InspectionRecordServiceImpl(InspectionRecordMapper inspectionRecordMapper) {
        this.inspectionRecordMapper = inspectionRecordMapper;
    }

    @Override
    public void recordDetection(Long userId,
                                String sourceType,
                                InferenceDetectResponse response,
                                String imageDataUrl,
                                String localizationDataUrl) {
        if (response == null || response.getData() == null) {
            return;
        }
        boolean hasAnomaly = normalizeAnomalyFlag(
                response.getData().getDescription(),
                response.getData().getHasAnomaly()
        );
        InspectionRecord record = new InspectionRecord();
        record.setUserId(userId);
        record.setSourceType(sourceType == null ? "upload" : sourceType);
        record.setDescription(response.getData().getDescription());
        record.setHasAnomaly(hasAnomaly ? 1 : 0);
        record.setReviewStatus(0);
        record.setImageUrl(imageDataUrl);
        record.setLocalizationImageUrl(localizationDataUrl);
        inspectionRecordMapper.insert(record);
    }

    @Override
    public PageResult<InspectionRecordResponse> list(Long userId,
                                                     Integer hasAnomaly,
                                                     Integer reviewStatus,
                                                     Integer reviewResult,
                                                     String keyword,
                                                     String startAt,
                                                     String endAt,
                                                     int page,
                                                     int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int offset = (safePage - 1) * safeSize;

        Integer total = inspectionRecordMapper.countPage(
                userId, hasAnomaly, reviewStatus, reviewResult, keyword, startAt, endAt);

        List<InspectionRecord> records = inspectionRecordMapper.selectPage(
                userId, hasAnomaly, reviewStatus, reviewResult, keyword, startAt, endAt, offset, safeSize);

        List<InspectionRecordResponse> responses = records.stream().map(this::toResponse).collect((Collectors.toList()));

        PageResult<InspectionRecordResponse> result = new PageResult<>();

        result.setPage(safePage);
        result.setSize(safeSize);
        result.setTotal(total == null ? 0 : total);
        result.setRecords(responses);

        return  result;
    }

    @Override
    public void reviewRecord(Long id, Integer reviewResult, String reviewNote, Long reviewerId) {
        Integer status = 1;
        Integer result = reviewResult == null ? 0 : reviewResult;
        inspectionRecordMapper.updateReview(
                id,
                status,
                result,
                reviewNote,
                reviewerId,
                LocalDateTime.now()
        );
    }

    private InspectionRecordResponse toResponse(InspectionRecord record) {
        InspectionRecordResponse response = new InspectionRecordResponse();
        response.setId(record.getId());
        response.setUserId(record.getUserId());
        response.setSourceType(record.getSourceType());
        response.setImageUrl(record.getImageUrl());
        response.setLocalizationImageUrl(record.getLocalizationImageUrl());
        response.setDescription(record.getDescription());
        response.setHasAnomaly(record.getHasAnomaly() != null && record.getHasAnomaly() == 1);
        response.setModelVersion(record.getModelVersion());
        response.setReviewStatus(record.getReviewStatus());
        response.setReviewResult(record.getReviewResult());
        response.setReviewNote(record.getReviewNote());
        response.setReviewedBy(record.getReviewedBy());
        response.setReviewedAt(record.getReviewedAt() == null ? null : record.getReviewedAt().format(FORMATTER));
        response.setCreatedAt(record.getCreatedAt() == null ? null : record.getCreatedAt().format(FORMATTER));
        return response;
    }

    private boolean normalizeAnomalyFlag(String description, Boolean fallback) {
        String text = description == null ? "" : description.toLowerCase();
        if (text.trim().isEmpty()) {
            return Boolean.TRUE.equals(fallback);
        }

        for (Pattern pattern : NEGATIVE_PATTERNS) {
            if (pattern.matcher(text).find()) {
                return false;
            }
        }

        for (Pattern pattern : POSITIVE_PATTERNS) {
            if (pattern.matcher(text).find()) {
                return true;
            }
        }

        return Boolean.TRUE.equals(fallback);
    }


}
