package com.websocket.chat_app.repository;

import com.websocket.chat_app.models.GroupChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Obiora on 02-Jul-2024 at 10:02
 */
public interface GroupChatMessageRepository extends JpaRepository<GroupChatMessage, Long> {
}
