package com.example.industrialdefectiveproductinspection.controller;

import com.example.industrialdefectiveproductinspection.dto.ApiResponse;
import com.example.industrialdefectiveproductinspection.dto.ChatSessionRequest;
import com.example.industrialdefectiveproductinspection.dto.ChatSessionResponse;
import com.example.industrialdefectiveproductinspection.dto.ChatSessionSummary;
import com.example.industrialdefectiveproductinspection.service.ChatSessionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat/sessions")
public class ChatSessionController {
    private final ChatSessionService chatSessionService;

    public ChatSessionController(ChatSessionService chatSessionService) {
        this.chatSessionService = chatSessionService;
    }

    @GetMapping
    public ApiResponse<List<ChatSessionSummary>> list(@RequestParam("userId") Long userId) {
        return ApiResponse.ok(chatSessionService.listByUser(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<ChatSessionResponse> get(@PathVariable("id") Long id) {
        return ApiResponse.ok(chatSessionService.get(id));
    }

    @PostMapping
    public ApiResponse<ChatSessionResponse> create(@RequestBody ChatSessionRequest request) {
        return ApiResponse.ok(chatSessionService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ChatSessionResponse> update(@PathVariable("id") Long id,
                                                   @RequestBody ChatSessionRequest request) {
        return ApiResponse.ok(chatSessionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        chatSessionService.delete(id);
        return ApiResponse.ok(null);
    }
}