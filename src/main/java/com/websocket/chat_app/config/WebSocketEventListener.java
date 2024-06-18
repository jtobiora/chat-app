package com.websocket.chat_app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

/**
 * Created by Obiora on 16-Jun-2024 at 10:40
 */

@Component
@Slf4j
public class WebSocketEventListener {

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class);
        if (headerAccessor != null && StompCommand.SUBSCRIBE.equals(headerAccessor.getCommand())) {
            String sessionId = headerAccessor.getSessionId();
            StompHeaders headers = (StompHeaders) headerAccessor.getMessageHeaders().get(StompHeaders.class);
            String destination = headerAccessor.getDestination();

            log.info("New subscription: sessionId={}, destination={}", sessionId, destination);
        }
    }

}
