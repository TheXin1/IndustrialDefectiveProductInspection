package com.example.industrialdefectiveproductinspection.mapper;

import com.example.industrialdefectiveproductinspection.domain.ChatSession;
import com.example.industrialdefectiveproductinspection.dto.ChatSessionSummary;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatSessionMapper {
    int insert(ChatSession session);

    int update(ChatSession session);

    int deleteById(@Param("id") Long id);

    ChatSession selectById(@Param("id") Long id);

    List<ChatSessionSummary> selectSummaryByUserId(@Param("userId") Long userId);
}