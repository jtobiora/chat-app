package com.websocket.chat_app.service;

import com.websocket.chat_app.models.GroupChatMessage;
import com.websocket.chat_app.repository.GroupChatMessageRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Created by Obiora on 02-Jul-2024 at 10:03
 */
@Service
@AllArgsConstructor
public class GroupChatService {

    private final GroupChatMessageRepository groupChatMessageRepository;

    public GroupChatMessage saveMessage (GroupChatMessage message) {
        return groupChatMessageRepository.save(message);
    }
}
