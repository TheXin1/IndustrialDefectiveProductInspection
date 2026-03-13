package com.example.industrialdefectiveproductinspection.mapper;

import com.example.industrialdefectiveproductinspection.domain.InspectionRecord;
import org.apache.ibatis.annotations.Param;

public interface InspectionRecordMapper {
    int insert(InspectionRecord record);

    Integer countTodayTotal();

    Integer countTodayAnomaly();

    Integer countAnomalySinceMinutes(@Param("minutes") int minutes);
}
