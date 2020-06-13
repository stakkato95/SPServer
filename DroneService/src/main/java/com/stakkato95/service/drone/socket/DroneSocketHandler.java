package com.stakkato95.service.drone.socket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stakkato95.service.drone.socket.transport.model.DroneInfo;
import com.stakkato95.service.drone.model.UnregisteredDrone;
import com.stakkato95.service.drone.socket.transport.Message;
import com.stakkato95.service.drone.socket.transport.MessageTemp;
import com.stakkato95.service.drone.socket.transport.MessageType;
import com.stakkato95.service.drone.socket.transport.model.Registration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DroneSocketHandler extends TextWebSocketHandler {

    private List<WebSocketSession> sessions;
    private final ObjectMapper objectMapper;
    private final MongoTemplate mongoTemplate;

    public DroneSocketHandler(MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
        sessions = new ArrayList<>();
    }

    public <T> void sendMessage(T payload, MessageType type) throws IOException {
        Message<T> message = new Message<>();
        message.messageType = type;
        message.payload = payload;

        String json = objectMapper.writeValueAsString(message);
        sessions.get(0).sendMessage(new TextMessage(json));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        this.sessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        System.out.println(message.getPayload());

        MessageTemp mTemp = objectMapper.readValue(message.getPayload(), MessageTemp.class);
        if (mTemp.messageType == MessageType.SHOW_UP) {
            Message<DroneInfo> m = objectMapper.readValue(message.getPayload(), new TypeReference<>() { });

            UnregisteredDrone info = new UnregisteredDrone();
            info.ip = m.payload.ip;
            info.position = m.payload.position;
            info.showUpTime = new Date();
            mongoTemplate.save(info);

            session.sendMessage(message);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        this.sessions.remove(session);
    }
}
