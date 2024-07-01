package com.websocket.chat_app.service;


import com.websocket.chat_app.models.ChatGroup;
import com.websocket.chat_app.models.Status;
import com.websocket.chat_app.models.User;
import com.websocket.chat_app.repository.GroupRepository;
import com.websocket.chat_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public void saveUser(User user) {
        //find the user in the database. If they exist, update their status to online if they do not create them
        Optional<User> userOptional = userRepository.findUserByNickName(user.getNickName());
        User u = null;
        if (userOptional.isPresent()) {
            u =  userOptional.get();
            u.setStatus(Status.ONLINE);
            userRepository.save(u);
        } else {
            user.setStatus(Status.ONLINE);
            userRepository.save(user);
        }
    }

    public void disconnect(User user) {
        var storedUser = userRepository.findUserByNickName(user.getNickName()).orElse(null);
        if (storedUser != null) {
            storedUser.setStatus(Status.OFFLINE);
            userRepository.save(storedUser);
        }
    }

    public List<User> findConnectedUsers() {
        return userRepository.findAllByStatus(Status.ONLINE);
    }
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findByNickname(String nickname) {
        return userRepository.findUserByNickName(nickname).orElseThrow(() -> new RuntimeException());
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public void addUserToGroup(String nickName, Long groupId) {
        User user = userRepository.findUserByNickName(nickName).orElseThrow(() -> new RuntimeException("User not found"));
        ChatGroup group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        user.getGroups().add(group);
        userRepository.save(user);
    }

    public Set<ChatGroup> getUserGroups(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return user.getGroups();
    }
}
