package com.example.industrialdefectiveproductinspection.service;

import com.example.industrialdefectiveproductinspection.dto.ChatSessionRequest;
import com.example.industrialdefectiveproductinspection.dto.ChatSessionResponse;
import com.example.industrialdefectiveproductinspection.dto.ChatSessionSummary;

import java.util.List;

public interface ChatSessionService {
    ChatSessionResponse create(ChatSessionRequest request);

    ChatSessionResponse update(Long id, ChatSessionRequest request);

    ChatSessionResponse get(Long id);

    List<ChatSessionSummary> listByUser(Long userId);

    void delete(Long id);
}