package com.websocket.chat_app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * Created by Obiora on 16-Jun-2024 at 10:42
 */

@Component
@Slf4j
public class WebSocketChannelInterceptor implements ChannelInterceptor {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (headerAccessor != null) {
            StompCommand command = headerAccessor.getCommand();
            if (command != null) {
                switch (command) {
                    case CONNECT:
                        log.info("STOMP Connect: sessionId={}, headers={}", headerAccessor.getSessionId(), headerAccessor.toNativeHeaderMap());
                        break;
                    case CONNECTED:
                        log.info("STOMP Connected: sessionId={}, headers={}", headerAccessor.getSessionId(), headerAccessor.toNativeHeaderMap());
                        break;
                    case SUBSCRIBE:
                        log.info("STOMP Subscribe: sessionId={}, destination={}, headers={}", headerAccessor.getSessionId(), headerAccessor.getDestination(), headerAccessor.toNativeHeaderMap());
                        break;
                    case UNSUBSCRIBE:
                        log.info("STOMP Unsubscribe: sessionId={}, destination={}, headers={}", headerAccessor.getSessionId(), headerAccessor.getDestination(), headerAccessor.toNativeHeaderMap());
                        break;
                    case DISCONNECT:
                        log.info("STOMP Disconnect: sessionId={}, headers={}", headerAccessor.getSessionId(), headerAccessor.toNativeHeaderMap());
                        break;
                    default:
                        log.info("STOMP Command: command={}, sessionId={}, headers={}", command, headerAccessor.getSessionId(), headerAccessor.toNativeHeaderMap());
                        break;
                }
            }
        }

        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        // Additional logging or handling if needed
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (headerAccessor != null) {
            StompCommand command = headerAccessor.getCommand();
            if (command != null) {
                switch (command) {
                    case CONNECT:
                        log.info("After Completion - STOMP Connect: sessionId={}, headers={}", headerAccessor.getSessionId(), headerAccessor.toNativeHeaderMap());
                        break;
                    case CONNECTED:
                        log.info("After Completion - STOMP Connected: sessionId={}, headers={}", headerAccessor.getSessionId(), headerAccessor.toNativeHeaderMap());
                        break;
                    case SUBSCRIBE:
                        log.info("After Completion - STOMP Subscribe: sessionId={}, destination={}, headers={}", headerAccessor.getSessionId(), headerAccessor.getDestination(), headerAccessor.toNativeHeaderMap());
                        break;
                    case UNSUBSCRIBE:
                        log.info("After Completion - STOMP Unsubscribe: sessionId={}, destination={}, headers={}", headerAccessor.getSessionId(), headerAccessor.getDestination(), headerAccessor.toNativeHeaderMap());
                        break;
                    case DISCONNECT:
                        log.info("After Completion - STOMP Disconnect: sessionId={}, headers={}", headerAccessor.getSessionId(), headerAccessor.toNativeHeaderMap());
                        break;
                    default:
                        log.info("After Completion - STOMP Command: command={}, sessionId={}, headers={}", command, headerAccessor.getSessionId(), headerAccessor.toNativeHeaderMap());
                        break;
                }
            }
        }

        if (ex != null) {
            log.error("Exception occurred while sending a message: {}", ex.getMessage(), ex);
        } else {
            log.info("Message sent successfully: sessionId={}", headerAccessor != null ? headerAccessor.getSessionId() : "unknown");
        }
    }


    @Override
    public boolean preReceive(MessageChannel channel) {
        return true;
    }

    @Override
    public Message<?> postReceive(Message<?> message, MessageChannel channel) {
        return message;
    }

    @Override
    public void afterReceiveCompletion(Message<?> message, MessageChannel channel, Exception ex) {
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (headerAccessor != null) {
            StompCommand command = headerAccessor.getCommand();
            if (command != null) {
                switch (command) {
                    case CONNECT:
                        log.info("After Receive Completion - STOMP Connect: sessionId={}, headers={}", headerAccessor.getSessionId(), headerAccessor.toNativeHeaderMap());
                        break;
                    case CONNECTED:
                        log.info("After Receive Completion - STOMP Connected: sessionId={}, headers={}", headerAccessor.getSessionId(), headerAccessor.toNativeHeaderMap());
                        break;
                    case SUBSCRIBE:
                        log.info("After Receive Completion - STOMP Subscribe: sessionId={}, destination={}, headers={}", headerAccessor.getSessionId(), headerAccessor.getDestination(), headerAccessor.toNativeHeaderMap());
                        break;
                    case UNSUBSCRIBE:
                        log.info("After Receive Completion - STOMP Unsubscribe: sessionId={}, destination={}, headers={}", headerAccessor.getSessionId(), headerAccessor.getDestination(), headerAccessor.toNativeHeaderMap());
                        break;
                    case DISCONNECT:
                        log.info("After Receive Completion - STOMP Disconnect: sessionId={}, headers={}", headerAccessor.getSessionId(), headerAccessor.toNativeHeaderMap());
                        break;
                    default:
                        log.info("After Receive Completion - STOMP Command: command={}, sessionId={}, headers={}", command, headerAccessor.getSessionId(), headerAccessor.toNativeHeaderMap());
                        break;
                }
            }
        }

        if (ex != null) {
            log.error("Exception occurred while receiving a message: {}", ex.getMessage(), ex);
        } else {
            log.info("Message received and processed successfully: sessionId={}", headerAccessor != null ? headerAccessor.getSessionId() : "unknown");
        }
    }
}
