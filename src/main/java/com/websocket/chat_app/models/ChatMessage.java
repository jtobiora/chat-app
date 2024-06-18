package com.websocket.chat_app.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by Obiora on 18-Jun-2024 at 01:52
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
//@Document
@Entity
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String chatId;
    private String senderId;
    private String recipientId;
    private String content;
    private Date timestamp;

//    @Column(name = "message_status")
//    @Enumerated(EnumType.STRING)
//    private MessageStatus messageStatus;
}

