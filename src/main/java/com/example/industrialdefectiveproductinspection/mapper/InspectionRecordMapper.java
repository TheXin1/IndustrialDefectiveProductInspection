package com.example.industrialdefectiveproductinspection.mapper;

import com.example.industrialdefectiveproductinspection.domain.InspectionRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface InspectionRecordMapper {
    int insert(InspectionRecord record);

    Integer countTodayTotal();

    Integer countTodayAnomaly();

    Integer countAnomalySinceMinutes(@Param("minutes") int minutes);

    List<InspectionRecord> selectPage(@Param("userId") Long userId,
                                      @Param("hasAnomaly") Integer hasAnomaly,
                                      @Param("reviewStatus") Integer reviewStatus,
                                      @Param("reviewResult") Integer reviewResult,
                                      @Param("keyword") String keyword,
                                      @Param("startAt") String startAt,
                                      @Param("endAt") String endAt,
                                      @Param("offset") int offset,
                                      @Param("limit") int limit);

    Integer countPage(@Param("userId") Long userId,
                      @Param("hasAnomaly") Integer hasAnomaly,
                      @Param("reviewStatus") Integer reviewStatus,
                      @Param("reviewResult") Integer reviewResult,
                      @Param("keyword") String keyword,
                      @Param("startAt") String startAt,
                      @Param("endAt") String endAt);

    int updateReview(@Param("id") Long id,
                     @Param("reviewStatus") Integer reviewStatus,
                     @Param("reviewResult") Integer reviewResult,
                     @Param("reviewNote") String reviewNote,
                     @Param("reviewedBy") Long reviewedBy,
                     @Param("reviewedAt") java.time.LocalDateTime reviewedAt);
}
