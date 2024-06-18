package com.websocket.chat_app.repository;

import com.websocket.chat_app.models.Status;
import com.websocket.chat_app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    List<User> findAllByStatus(Status status);
    Optional<User> findUserByNickName(String nickname);
}
