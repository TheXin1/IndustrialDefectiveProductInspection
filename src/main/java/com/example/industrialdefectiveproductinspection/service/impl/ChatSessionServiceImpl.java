package com.example.industrialdefectiveproductinspection.service.impl;

import com.example.industrialdefectiveproductinspection.domain.ChatMessage;
import com.example.industrialdefectiveproductinspection.domain.ChatSession;
import com.example.industrialdefectiveproductinspection.dto.ChatMessageDto;
import com.example.industrialdefectiveproductinspection.dto.ChatSessionRequest;
import com.example.industrialdefectiveproductinspection.dto.ChatSessionResponse;
import com.example.industrialdefectiveproductinspection.dto.ChatSessionSummary;
import com.example.industrialdefectiveproductinspection.exception.ServiceException;
import com.example.industrialdefectiveproductinspection.mapper.ChatMessageMapper;
import com.example.industrialdefectiveproductinspection.mapper.ChatSessionMapper;
import com.example.industrialdefectiveproductinspection.service.ChatSessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatSessionServiceImpl implements ChatSessionService {
    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final ObjectMapper objectMapper;

    public ChatSessionServiceImpl(ChatSessionMapper chatSessionMapper,
                                  ChatMessageMapper chatMessageMapper,
                                  ObjectMapper objectMapper) {
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ChatSessionResponse create(ChatSessionRequest request) {
        ChatSession session = toEntity(request);
        chatSessionMapper.insert(session);
        replaceMessages(session.getId(), request.getMessages());
        return get(session.getId());
    }

    @Override
    @Transactional
    public ChatSessionResponse update(Long id, ChatSessionRequest request) {
        ChatSession session = toEntity(request);
        session.setId(id);
        chatSessionMapper.update(session);
        replaceMessages(id, request.getMessages());
        return get(id);
    }

    @Override
    public ChatSessionResponse get(Long id) {
        ChatSession session = chatSessionMapper.selectById(id);
        if (session == null) {
            throw new ServiceException("未找到对话数据");
        }
        List<ChatMessage> messages = chatMessageMapper.selectBySessionId(id);
        return toResponse(session, messages);
    }

    @Override
    public List<ChatSessionSummary> listByUser(Long userId) {
        if (userId == null) {
            throw new ServiceException("非法用户Id");
        }
        return chatSessionMapper.selectSummaryByUserId(userId);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        chatMessageMapper.deleteBySessionId(id);
        chatSessionMapper.deleteById(id);
    }

    private void replaceMessages(Long sessionId, List<ChatMessageDto> messages) {
        chatMessageMapper.deleteBySessionId(sessionId);
        if (messages == null || messages.isEmpty()) {
            return;
        }
        List<ChatMessage> entities = new ArrayList<>();
        for (ChatMessageDto dto : messages) {
            if (dto == null) {
                continue;
            }
            ChatMessage message = new ChatMessage();
            message.setSessionId(sessionId);
            message.setRole(dto.getRole());
            message.setContent(dto.getContent());
            message.setCreatedAt(LocalDateTime.now());
            entities.add(message);
        }
        if (!entities.isEmpty()) {
            chatMessageMapper.insertBatch(sessionId, entities);
        }
    }

    private ChatSession toEntity(ChatSessionRequest request) {
        if (request == null) {
            throw new ServiceException("非法请求");
        }
        ChatSession session = new ChatSession();
        session.setUserId(request.getUserId());
        session.setTitle(request.getTitle());
        session.setPrompt(request.getPrompt());
        session.setMaxTgtLen(request.getMaxTgtLen());
        session.setTopP(request.getTopP());
        session.setTemperature(request.getTemperature());
        session.setImageDataUrl(request.getImageDataUrl());
        session.setNormalDataUrl(request.getNormalDataUrl());
        session.setResultJson(writeJson(request.getResult()));
        return session;
    }

    private ChatSessionResponse toResponse(ChatSession session, List<ChatMessage> messages) {
        ChatSessionResponse response = new ChatSessionResponse();
        response.setId(session.getId());
        response.setUserId(session.getUserId());
        response.setTitle(session.getTitle());
        response.setPrompt(session.getPrompt());
        response.setMaxTgtLen(session.getMaxTgtLen());
        response.setTopP(session.getTopP());
        response.setTemperature(session.getTemperature());
        response.setImageDataUrl(session.getImageDataUrl());
        response.setNormalDataUrl(session.getNormalDataUrl());
        response.setResult(readJson(session.getResultJson()));
        response.setCreatedAt(formatTime(session.getCreatedAt()));
        response.setUpdatedAt(formatTime(session.getUpdatedAt()));
        response.setMessages(toMessageDtos(messages));
        return response;
    }

    private String formatTime(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private List<ChatMessageDto> toMessageDtos(List<ChatMessage> messages) {
        List<ChatMessageDto> list = new ArrayList<>();
        if (messages == null) {
            return list;
        }
        for (ChatMessage message : messages) {
            ChatMessageDto dto = new ChatMessageDto();
            dto.setRole(message.getRole());
            dto.setContent(message.getContent());
            dto.setTime(formatTime(message.getCreatedAt()));
            list.add(dto);
        }
        return list;
    }

    private String writeJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ServiceException("json序列化错误");
        }
    }

    private Object readJson(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(value, Object.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}