package com.websocket.chat_app.controller;

import com.websocket.chat_app.models.User;
import com.websocket.chat_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    /*
    * When the user is connected, the payload is sent to this endpoint. The connected user is saved and his
    * connection is broadcast to the public topic `/topic/public`so that he is seen by all those who have subscribed to the
    * public topic
    * */
    @MessageMapping("/user.addUser")
    //@SendTo("/user/public")
    public void addUser(@Payload User user) {
        userService.saveUser(user);
        simpMessagingTemplate.convertAndSend("/topic/public", user);
    }


    /*
     * When the user is disconnected from websocket, his disconnection is broadcast to the public topic `/topic/public`so that he is
     * seen to have left the chat by all those who have subscribed to the
     * public topic
     * */
    @MessageMapping("/user.disconnectUser")
    //@SendTo("/user/public")
    public void disconnectUser(@Payload User user) {
        userService.disconnect(user);
        simpMessagingTemplate.convertAndSend("/topic/public", user);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> findConnectedUsers() {
        return ResponseEntity.ok(userService.findConnectedUsers());
    }
}
