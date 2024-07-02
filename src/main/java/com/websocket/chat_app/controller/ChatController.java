package com.websocket.chat_app.controller;

import com.websocket.chat_app.models.*;
import com.websocket.chat_app.service.ChatMessageService;
import com.websocket.chat_app.service.GroupChatService;
import com.websocket.chat_app.service.UserService;
import com.websocket.chat_app.utils.EncryptionUtil;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Set;


/*
* 1. Message Sent to ActiveMQ Queue: When a message is generated, it is encrypted and sent to an ActiveMQ queue.
  2. Spring Application Consumes the Message: The Spring application listens for messages from this queue. Upon receiving a
* message, it decrypts the message content.
* 3. Forward Message to WebSocket Endpoint: The decrypted message is then
* forwarded to the appropriate WebSocket endpoint to be sent to the client.

* */
@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final JmsTemplate jmsTemplate;
    private final UserService userService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final GroupChatService groupChatService;

//    @MessageMapping("/chat")
//    public void processMessage(@Payload ChatMessage chatMessage) {
//        log.info("Sending chat message from {} to {}", chatMessage.getSenderId(), chatMessage.getRecipientId());
//        ChatMessage savedMsg = chatMessageService.save(chatMessage);
//        // Decrypt the content before sending
//        String decryptedContent = EncryptionUtil.decrypt(savedMsg.getContent());
//
//        // Create a new ChatNotification with decrypted content
//        ChatNotification decryptedNotification = new ChatNotification(
//                savedMsg.getId(),
//                savedMsg.getSenderId(),
//                savedMsg.getRecipientId(),
//                decryptedContent
//        );
//        // Constructing dynamic queue name
//        String destination = String.format("/queue/%s/messages", chatMessage.getRecipientId());
//        log.info("Sending message to dynamic queue: {}", destination);
//        messagingTemplate.convertAndSend(destination, decryptedNotification);
//        log.info("Message sent to dynamic queue: {} successfully.", destination);
//    }

    @MessageMapping("/chat")
    public void processMessagetoActiveMQ(@Payload ChatMessage chatMessage) {
        log.info("Sending chat message from {} to {}", chatMessage.getSenderId(), chatMessage.getRecipientId());
        ChatMessage savedMsg = chatMessageService.save(chatMessage);
        ChatNotification notification = new ChatNotification(
                savedMsg.getId(),
                savedMsg.getSenderId(),
                savedMsg.getRecipientId(),
                savedMsg.getContent()
        );

        // Constructing dynamic queue name
        String userQueueName = String.format("queue.%s.messages", chatMessage.getRecipientId());
        log.info("Sending message to ActiveMQ queue: {}", userQueueName);

        jmsTemplate.convertAndSend(userQueueName, notification, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws JMSException {
                message.setStringProperty("JMSDestination", userQueueName);
                return message;
            }
        });
    }

    @GetMapping("/messages/{senderId}/{recipientId}")
    public ResponseEntity<List<ChatMessage>> findChatMessages(@PathVariable String senderId,
                                                 @PathVariable String recipientId) {
        log.info("GETTING MESSAGES for sender {} and receiver {} ........ ", senderId, recipientId);
        return ResponseEntity
                .ok(chatMessageService.findChatMessages(senderId, recipientId));
    }


    @JmsListener(destination = "queue.*.messages")
    public void receiveMessageFromActiveMQ(ChatNotification notification,
                                           @Header(name = "JMSDestination", required = false) String destination) {
        log.info("Received notification: {}", notification);
        if (destination != null) {
            log.info("Message destination: {}", destination);
            // Extract recipient ID from the destination
            String recipientId = destination.split("\\.")[1]; // Extract the recipientId between "queue." and ".messages"

            // Decrypt the content before sending
            String decryptedContent = EncryptionUtil.decrypt(notification.getContent());

            // Create a new ChatNotification with decrypted content
            ChatNotification decryptedNotification = new ChatNotification(
                    notification.getId(),
                    notification.getSenderId(),
                    notification.getRecipientId(),
                    decryptedContent
            );

            // Send message to the specific user's WebSocket queue
            String websocketDestination = String.format("/queue/%s/messages", recipientId);

//            The messagingTemplate.convertAndSend(websocketDestination, decryptedNotification) line is crucial because
//            it forwards the decrypted message to the specific WebSocket endpoint (e.g., /queue/user123/messages) where
//            the intended client is subscribed. This ensures that each user receives their messages in real-time
//            through their WebSocket connection.
            messagingTemplate.convertAndSend(websocketDestination, decryptedNotification);
        } else {
            log.warn("JMSDestination header is missing.");
        }
    }

    /*
     * When the user is connected, the payload is sent to this endpoint. The connected user is saved and his
     * connection is broadcast to the public topic `/topic/public`so that he is seen by all those who have subscribed to the
     * public topic
     * */
    @MessageMapping("/user.addUser")
    //@SendTo("/user/public")
    public void addUser(@Payload User user, SimpMessageHeaderAccessor headerAccessor) {
        userService.saveUser(user);
        simpMessagingTemplate.convertAndSend("/topic/public", user);

        // Add user to web socket session
        headerAccessor.getSessionAttributes().put("username", user.getNickName());

//        // Get groups of the user and subscribe to them
//        Set<ChatGroup> groups = userService.getUserGroups(user.getNickName());
//        for (ChatGroup group : groups) {
//            System.out.println("name of group " + group.getName());
//            //messagingTemplate.convertAndSend("/topic/groups/" + group.getName(), new ChatMessage());
//        }
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

//    @GetMapping("/messages/{senderId}/{recipientId}/count")
//    public ResponseEntity<Long> countNewMessages(
//            @PathVariable String senderId,
//            @PathVariable String recipientId) {
//
//        return ResponseEntity
//                .ok(chatMessageService.countNewMessages(senderId, recipientId));
//
//    }

    @MessageMapping("/chat.sendGroupMessage")
    public void sendMessage(@Payload GroupChatMessage chatMessage) {
        GroupChatMessage groupChatMessage = groupChatService.saveMessage(chatMessage);
        messagingTemplate.convertAndSend("/topic/groups/" + chatMessage.getGroupName(), groupChatMessage);
    }
}
