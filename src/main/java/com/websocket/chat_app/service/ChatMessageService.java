package com.websocket.chat_app.service;


import com.websocket.chat_app.models.ChatMessage;
import com.websocket.chat_app.models.MessageStatus;
import com.websocket.chat_app.repository.ChatMessageRepository;
import com.websocket.chat_app.utils.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository repository;
    private final ChatRoomService chatRoomService;

    public ChatMessage save(ChatMessage chatMessage) {
        var chatId = chatRoomService
                .getChatRoomId(chatMessage.getSenderId(), chatMessage.getRecipientId(), true)
                .orElseThrow(); // You can create your own dedicated exception
        chatMessage.setChatId(chatId);

        // Encrypt the message content before saving
        String encryptedContent = EncryptionUtil.encrypt(chatMessage.getContent());
        chatMessage.setContent(encryptedContent);

        repository.save(chatMessage);
        return chatMessage;
    }

//    public List<ChatMessage> findChatMessages(String senderId, String recipientId) {
//        var chatId = chatRoomService.getChatRoomId(senderId, recipientId, false);
//        return chatId.map(repository::findByChatId).orElse(new ArrayList<>());
//    }

    public List<ChatMessage> findChatMessages(String senderId, String recipientId) {
        var chatId = chatRoomService.getChatRoomId(senderId, recipientId, false);
        if (chatId.isPresent()) {
            List<ChatMessage> messages = repository.findByChatId(chatId.get());

            // Decrypt the message content before returning
            for (ChatMessage message : messages) {
                String decryptedContent = EncryptionUtil.decrypt(message.getContent());
                message.setContent(decryptedContent);
            }

            return messages;
        } else {
            return new ArrayList<>();
        }
    }

//    public long countNewMessages(String senderId, String recipientId) {
//        return repository.countBySenderIdAndRecipientIdAndStatus(
//                senderId, recipientId, MessageStatus.RECEIVED);
//    }
}
