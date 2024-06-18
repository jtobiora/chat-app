package com.websocket.chat_app.controller;

import com.websocket.chat_app.models.ChatGroup;
import com.websocket.chat_app.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Created by Obiora on 18-Jun-2024 at 03:16
 */
@RestController
@RequestMapping("/user-groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @PostMapping
    public ResponseEntity<ChatGroup> createGroup(@RequestBody ChatGroup group) {
        return ResponseEntity.ok(groupService.save(group));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatGroup> getGroupById(@PathVariable Long id) {
        Optional<ChatGroup> group = groupService.findById(id);
        return group.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ChatGroup>> getAllGroups() {
        return ResponseEntity.ok(groupService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.ok().build();
    }
}
