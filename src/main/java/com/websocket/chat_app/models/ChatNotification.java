package com.websocket.chat_app.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by Obiora on 18-Jun-2024 at 01:55
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatNotification implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String senderId;
    private String recipientId;
    private String content;
}
