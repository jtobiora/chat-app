package com.websocket.chat_app.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Obiora on 18-Jun-2024 at 01:55
 */
@Getter
@Setter
//@Document
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nickName;
    private String fullName;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;
}
