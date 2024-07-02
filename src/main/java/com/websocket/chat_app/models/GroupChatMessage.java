package com.websocket.chat_app.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by Obiora on 02-Jul-2024 at 08:55
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
//@Document
@Entity
@Table(name = "chat_group_message")
@Data
public class GroupChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String senderId;
    private String content;
    private String groupName;
    private Date timestamp;
}