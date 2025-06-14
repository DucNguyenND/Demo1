package com.example.doantn.repository;

import com.example.doantn.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    @Query("SELECT m FROM ChatMessage m WHERE " +
           "(m.sender.username = :username1 AND m.receiver.username = :username2) OR " +
           "(m.sender.username = :username2 AND m.receiver.username = :username1) " +
           "ORDER BY m.timestamp ASC")
    List<ChatMessage> findChatMessagesBetweenUsers(
            @Param("username1") String username1,
            @Param("username2") String username2);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.receiver.username = :username AND m.isRead = false")
    long countUnreadMessages(@Param("username") String username);

    @Query("UPDATE ChatMessage m SET m.isRead = true " +
           "WHERE m.sender.username = :senderUsername AND m.receiver.username = :receiverUsername AND m.isRead = false")
    void markMessagesAsRead(
            @Param("senderUsername") String senderUsername,
            @Param("receiverUsername") String receiverUsername);
} 