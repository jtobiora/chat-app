    package com.websocket.chat_app.controller;

    import com.websocket.chat_app.models.ChatGroup;
    import com.websocket.chat_app.models.User;
    import com.websocket.chat_app.service.UserService;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.http.ResponseEntity;
    import org.springframework.messaging.simp.SimpMessagingTemplate;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;
    import java.util.Optional;
    import java.util.Set;

    @RestController
    @RequiredArgsConstructor
    @RequestMapping("/users")
    @Slf4j
    public class UserController {

        private final UserService userService;
        private final SimpMessagingTemplate simpMessagingTemplate;

    //    /*
    //    * When the user is connected, the payload is sent to this endpoint. The connected user is saved and his
    //    * connection is broadcast to the public topic `/topic/public`so that he is seen by all those who have subscribed to the
    //    * public topic
    //    * */
    //    @MessageMapping("/user.addUser")
    //    //@SendTo("/user/public")
    //    public void addUser(@Payload User user) {
    //        userService.saveUser(user);
    //        simpMessagingTemplate.convertAndSend("/topic/public", user);
    //    }


    //    /*
    //     * When the user is disconnected from websocket, his disconnection is broadcast to the public topic `/topic/public`so that he is
    //     * seen to have left the chat by all those who have subscribed to the
    //     * public topic
    //     * */
    //    @MessageMapping("/user.disconnectUser")
    //    //@SendTo("/user/public")
    //    public void disconnectUser(@Payload User user) {
    //        userService.disconnect(user);
    //        simpMessagingTemplate.convertAndSend("/topic/public", user);
    //    }

        @GetMapping("/connected")
        public ResponseEntity<List<User>> findConnectedUsers() {
            return ResponseEntity.ok(userService.findConnectedUsers());
        }

        @GetMapping("/{id}")
        public ResponseEntity<User> getUserById(@PathVariable Long id) {
            Optional<User> user = userService.findById(id);
            return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        }

        @GetMapping()
        public ResponseEntity<List<User>> getAllUsers() {
            return ResponseEntity.ok(userService.findAll());
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        }

        @PostMapping("/{nickName}/groups/{groupId}")
        public ResponseEntity<Void> addUserToGroup(@PathVariable String nickName, @PathVariable Long groupId) {
            log.info("Adding User {} to group with id {}", nickName, groupId);
            userService.addUserToGroup(nickName, groupId);
            return ResponseEntity.ok().build();
        }

//        @GetMapping("/{userId}/groups")
//        public ResponseEntity<Set<ChatGroup>> getUserGroups(@PathVariable Long userId) {
//            return ResponseEntity.ok(userService.getUserGroups(userId));
//        }

        @GetMapping("/groups/{nickName}")
        public ResponseEntity<Set<ChatGroup>> getGroupsAUserBelongTo(@PathVariable String nickName) {
            return ResponseEntity.ok(userService.getUserGroups(nickName));
        }
    }
