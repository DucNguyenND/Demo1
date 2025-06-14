package com.example.doantn.controller;

import com.example.doantn.dto.chat.ChatMessage;
import com.example.doantn.dto.chat.ChatRoom;
import com.example.doantn.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Chat API endpoints")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    @Operation(summary = "Gửi tin nhắn", description = "Gửi tin nhắn đến người dùng khác")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        ChatMessage savedMessage = chatService.saveMessage(chatMessage);
        
        // Gửi tin nhắn đến người nhận
        messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiverUsername(),
                "/queue/messages",
                savedMessage
        );
        
        // Gửi tin nhắn đến người gửi (để cập nhật UI)
        messagingTemplate.convertAndSendToUser(
                chatMessage.getSenderUsername(),
                "/queue/messages",
                savedMessage
        );
    }

    @MessageMapping("/chat.addUser")
    @Operation(summary = "Thêm người dùng vào chat", description = "Thêm người dùng vào phiên chat và gửi thông báo")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Lưu username vào session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSenderUsername());
        
        // Gửi thông báo người dùng tham gia
        messagingTemplate.convertAndSend("/topic/public", chatMessage);
    }

    @GetMapping("/messages/{username}")
    @Operation(summary = "Lấy tin nhắn", description = "Lấy lịch sử tin nhắn giữa hai người dùng")
    public ResponseEntity<List<ChatMessage>> getMessages(
            @PathVariable String username,
            Authentication authentication) {
        String currentUsername = authentication.getName();
        return ResponseEntity.ok(chatService.getChatMessages(currentUsername, username));
    }

    @GetMapping("/rooms")
    @Operation(summary = "Lấy danh sách phòng chat", description = "Lấy danh sách các phòng chat của người dùng")
    public ResponseEntity<List<ChatRoom>> getChatRooms(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(chatService.getChatRooms(username));
    }

    @PostMapping("/rooms/{username}")
    @Operation(summary = "Tạo phòng chat mới", description = "Tạo phòng chat mới với người dùng khác")
    public ResponseEntity<ChatRoom> createChatRoom(
            @PathVariable String username,
            Authentication authentication) {
        String currentUsername = authentication.getName();
        return ResponseEntity.ok(chatService.createChatRoom(currentUsername, username));
    }

    @PutMapping("/messages/read/{username}")
    @Operation(summary = "Đánh dấu tin nhắn đã đọc", description = "Đánh dấu tất cả tin nhắn từ người dùng là đã đọc")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable String username,
            Authentication authentication) {
        String currentUsername = authentication.getName();
        chatService.markMessagesAsRead(username, currentUsername);
        return ResponseEntity.ok().build();
    }
} 