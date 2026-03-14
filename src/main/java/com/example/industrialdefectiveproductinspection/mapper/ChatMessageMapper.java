package com.example.industrialdefectiveproductinspection.mapper;

import com.example.industrialdefectiveproductinspection.domain.ChatMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatMessageMapper {

    int insertBatch(@Param("sessionId") Long sessionId, @Param("messages") List<ChatMessage> messages);

    int deleteBySessionId(@Param("sessionId") Long sessionId);

    List<ChatMessage> selectBySessionId(@Param("sessionId") Long sessionId);
}