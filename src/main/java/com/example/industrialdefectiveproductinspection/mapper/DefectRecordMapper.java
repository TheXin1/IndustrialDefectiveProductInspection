package com.example.industrialdefectiveproductinspection.mapper;

import com.example.industrialdefectiveproductinspection.domain.DefectRecord;
import org.apache.ibatis.annotations.Param;

public interface DefectRecordMapper {

    DefectRecord selectById(@Param("id") Long id);

    int insert(DefectRecord record);
}
