package com.example.doantn.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private Long id;
    private String senderUsername;
    private String receiverUsername;
    private String content;
    private MessageType type;
    private LocalDateTime timestamp;
    private boolean isRead;

    public enum MessageType {
        CHAT,    // Tin nhắn thông thường
        JOIN,    // Thông báo người dùng tham gia
        LEAVE    // Thông báo người dùng rời đi
    }
} 