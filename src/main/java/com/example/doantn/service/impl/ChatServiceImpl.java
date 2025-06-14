package com.example.doantn.service.impl;

import com.example.doantn.dto.chat.ChatMessage;
import com.example.doantn.dto.chat.ChatRoom;
import com.example.doantn.entity.Role;
import com.example.doantn.entity.User;
import com.example.doantn.repository.ChatMessageRepository;
import com.example.doantn.repository.ChatRoomRepository;
import com.example.doantn.repository.UserRepository;
import com.example.doantn.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ChatMessage saveMessage(ChatMessage chatMessage) {
        User sender = userRepository.findByUsername(chatMessage.getSenderUsername())
                .orElseThrow(() -> new RuntimeException("Người gửi không tồn tại"));
        User receiver = userRepository.findByUsername(chatMessage.getReceiverUsername())
                .orElseThrow(() -> new RuntimeException("Người nhận không tồn tại"));

        // Kiểm tra xem có phải chat giữa admin và user không
        if (!isValidChatPair(sender, receiver)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ được phép chat với admin");
        }

        com.example.doantn.entity.ChatMessage message = com.example.doantn.entity.ChatMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content(chatMessage.getContent())
                .type(com.example.doantn.entity.ChatMessage.MessageType.valueOf(chatMessage.getType().name()))
                .timestamp(LocalDateTime.now())
                .isRead(false)
                .build();

        message = chatMessageRepository.save(message);

        // Cập nhật phòng chat
        String chatId = getChatId(sender.getUsername(), receiver.getUsername());
        com.example.doantn.entity.ChatRoom chatRoom = chatRoomRepository.findByChatId(chatId)
                .orElseGet(() -> {
                    if (!isValidChatPair(sender, receiver)) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ được phép chat với admin");
                    }
                    return createChatRoomEntity(sender, receiver);
                });

        chatRoom.setLastMessage(chatMessage.getContent());
        chatRoom.setUnreadCount(chatRoom.getUnreadCount() + 1);
        chatRoomRepository.save(chatRoom);

        return convertToDto(message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> getChatMessages(String senderUsername, String receiverUsername) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Người gửi không tồn tại"));
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new RuntimeException("Người nhận không tồn tại"));

        if (!isValidChatPair(sender, receiver)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ được phép chat với admin");
        }

        return chatMessageRepository.findChatMessagesBetweenUsers(senderUsername, receiverUsername)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoom> getChatRooms(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // Nếu là admin, lấy tất cả phòng chat
        if (user.getRole() == Role.ROLE_ADMIN) {
            return chatRoomRepository.findAll().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }

        // Nếu là user thường, chỉ lấy phòng chat với admin
        return chatRoomRepository.findAllByUsername(username).stream()
                .filter(room -> room.getSender().getRole() == Role.ROLE_ADMIN || 
                              room.getReceiver().getRole() == Role.ROLE_ADMIN)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChatRoom createChatRoom(String senderUsername, String receiverUsername) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Người gửi không tồn tại"));
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new RuntimeException("Người nhận không tồn tại"));

        if (!isValidChatPair(sender, receiver)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ được phép chat với admin");
        }

        return convertToDto(createChatRoomEntity(sender, receiver));
    }

    @Override
    @Transactional
    public void markMessagesAsRead(String senderUsername, String receiverUsername) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Người gửi không tồn tại"));
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new RuntimeException("Người nhận không tồn tại"));

        if (!isValidChatPair(sender, receiver)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ được phép chat với admin");
        }

        chatMessageRepository.markMessagesAsRead(senderUsername, receiverUsername);
        
        String chatId = getChatId(senderUsername, receiverUsername);
        chatRoomRepository.findByChatId(chatId).ifPresent(chatRoom -> {
            chatRoom.setUnreadCount(0);
            chatRoomRepository.save(chatRoom);
        });
    }

    private boolean isValidChatPair(User user1, User user2) {
        // Chỉ cho phép chat giữa admin và user thường
        return (user1.getRole() == Role.ROLE_ADMIN && user2.getRole() == Role.ROLE_USER) ||
               (user1.getRole() == Role.ROLE_USER && user2.getRole() == Role.ROLE_ADMIN);
    }

    private String getChatId(String username1, String username2) {
        return username1.compareTo(username2) < 0 
                ? username1 + "_" + username2 
                : username2 + "_" + username1;
    }

    private com.example.doantn.entity.ChatRoom createChatRoomEntity(User sender, User receiver) {
        String chatId = getChatId(sender.getUsername(), receiver.getUsername());
        return com.example.doantn.entity.ChatRoom.builder()
                .chatId(chatId)
                .sender(sender)
                .receiver(receiver)
                .unreadCount(0)
                .build();
    }

    private ChatMessage convertToDto(com.example.doantn.entity.ChatMessage message) {
        return ChatMessage.builder()
                .id(message.getId())
                .senderUsername(message.getSender().getUsername())
                .receiverUsername(message.getReceiver().getUsername())
                .content(message.getContent())
                .type(ChatMessage.MessageType.valueOf(message.getType().name()))
                .timestamp(message.getTimestamp())
                .isRead(message.isRead())
                .build();
    }

    private ChatRoom convertToDto(com.example.doantn.entity.ChatRoom chatRoom) {
        return ChatRoom.builder()
                .id(chatRoom.getId().toString())
                .chatId(chatRoom.getChatId())
                .senderUsername(chatRoom.getSender().getUsername())
                .receiverUsername(chatRoom.getReceiver().getUsername())
                .lastMessage(chatRoom.getLastMessage())
                .unreadCount(chatRoom.getUnreadCount())
                .build();
    }
} 