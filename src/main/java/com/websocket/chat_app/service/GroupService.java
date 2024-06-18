package com.websocket.chat_app.service;


import com.websocket.chat_app.models.ChatGroup;
import com.websocket.chat_app.repository.GroupRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Created by Obiora on 17-Jun-2024 at 19:24
 */
@Service
@Transactional
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;

    public ChatGroup save(ChatGroup group) {
        return groupRepository.save(group);
    }

    public Optional<ChatGroup> findById(Long id) {
        return groupRepository.findById(id);
    }

    public List<ChatGroup> findAll() {
        return groupRepository.findAll();
    }

    public void deleteGroup(Long id) {
        groupRepository.deleteById(id);
    }
}