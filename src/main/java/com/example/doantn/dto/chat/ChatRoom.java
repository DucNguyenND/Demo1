package com.example.doantn.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoom {
    private String id;
    private String chatId; // ID của cuộc trò chuyện (username1_username2)
    private String senderUsername;
    private String receiverUsername;
    private String lastMessage;
    private long unreadCount;
} 