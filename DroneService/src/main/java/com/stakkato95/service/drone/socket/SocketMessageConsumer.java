package com.stakkato95.service.drone.socket;

import org.springframework.web.socket.WebSocketSession;

@FunctionalInterface
public interface SocketMessageConsumer {
    void consume(WebSocketSession session, String payload) throws Exception;
}
