package com.example.doantn.repository;

import com.example.doantn.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    Optional<ChatRoom> findByChatId(String chatId);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.sender.username = :username OR cr.receiver.username = :username")
    List<ChatRoom> findAllByUsername(@Param("username") String username);

    @Query("SELECT cr FROM ChatRoom cr WHERE " +
           "(cr.sender.username = :username1 AND cr.receiver.username = :username2) OR " +
           "(cr.sender.username = :username2 AND cr.receiver.username = :username1)")
    Optional<ChatRoom> findByUsernames(
            @Param("username1") String username1,
            @Param("username2") String username2);
} 