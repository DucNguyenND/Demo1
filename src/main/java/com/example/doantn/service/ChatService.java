package com.example.doantn.service;

import com.example.doantn.dto.chat.ChatMessage;
import com.example.doantn.dto.chat.ChatRoom;

import java.util.List;

public interface ChatService {
    ChatMessage saveMessage(ChatMessage chatMessage);
    List<ChatMessage> getChatMessages(String senderUsername, String receiverUsername);
    List<ChatRoom> getChatRooms(String username);
    ChatRoom createChatRoom(String senderUsername, String receiverUsername);
    void markMessagesAsRead(String senderUsername, String receiverUsername);
} 