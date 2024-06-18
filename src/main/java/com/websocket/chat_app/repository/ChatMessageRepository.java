package com.websocket.chat_app.repository;

import com.websocket.chat_app.models.ChatMessage;
import com.websocket.chat_app.models.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    List<ChatMessage> findByChatId(String chatId);

//    @Query("SELECT c FROM ChatMessage c WHERE c.senderId = ?1 AND c.recipientId = ?2 AND c.messageStatus=?3")
//    long countBySenderIdAndRecipientIdAndStatus(
//            String senderId, String recipientId, MessageStatus status);
}
