package com.example.doantn.controller;

import com.example.doantn.dto.chat.ChatMessage;
import com.example.doantn.dto.chat.ChatRoom;
import com.example.doantn.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Chat API endpoints")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage, Principal principal) {
        log.info("Received message: {} from {}", chatMessage, principal != null ? principal.getName() : "anonymous");
        if (principal != null) {
            chatMessage.setSenderUsername(principal.getName());
        }
        ChatMessage saved = chatService.saveMessage(chatMessage);
        return saved;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/messages")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, 
                             SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSenderUsername());
        log.info("User added: {}", chatMessage.getSenderUsername());
        return chatMessage;
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

    // Gửi tin nhắn từ user tới admin
    @MessageMapping("/chat.toAdmin")
    public void sendToAdmin(@Payload String message, SimpMessageHeaderAccessor headerAccessor) {
        // Giả sử admin có username là "admin"
        messagingTemplate.convertAndSendToUser("admin", "/queue/messages", message);
    }

    // Gửi tin nhắn từ admin tới user
    @MessageMapping("/chat.toUser")
    public void sendToUser(@Payload String message, SimpMessageHeaderAccessor headerAccessor) {
        // Lấy username người nhận từ header hoặc message (tùy bạn thiết kế)
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username != null) {
            messagingTemplate.convertAndSendToUser(username, "/queue/messages", message);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<String>> getAllUsersExceptAdmin() {
        // Lấy danh sách user (trừ admin)
        List<String> users = chatService.getAllUsernamesExceptAdmin();
        return ResponseEntity.ok(users);
    }
} 