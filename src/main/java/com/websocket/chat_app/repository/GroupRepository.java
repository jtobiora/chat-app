package com.websocket.chat_app.repository;

import com.websocket.chat_app.models.ChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Obiora on 18-Jun-2024 at 03:13
 */
public interface GroupRepository extends JpaRepository<ChatGroup, Long> {
}
