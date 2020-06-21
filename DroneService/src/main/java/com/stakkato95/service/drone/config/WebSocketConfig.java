package com.stakkato95.service.drone.config;

import com.stakkato95.service.drone.socket.DroneSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private static final String ENDPOINT = "/socket/droneSocket";

    private final DroneSocketHandler droneSocketHandler;

    public WebSocketConfig(DroneSocketHandler droneSocketHandler) {
        this.droneSocketHandler = droneSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(droneSocketHandler, ENDPOINT);
    }
}