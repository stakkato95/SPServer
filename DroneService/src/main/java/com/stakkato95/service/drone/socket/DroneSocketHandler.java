package com.stakkato95.service.drone.socket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stakkato95.service.drone.socket.transport.model.response.DroneInfo;
import com.stakkato95.service.drone.model.drone.UnregisteredDrone;
import com.stakkato95.service.drone.socket.transport.Message;
import com.stakkato95.service.drone.socket.transport.MessageTemp;
import com.stakkato95.service.drone.socket.transport.MessageType;
import com.stakkato95.service.drone.socket.transport.model.response.StartSessionAck;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;

public class DroneSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final MongoTemplate mongoTemplate;
    private List<WebSocketSession> sessions;
    private Map<MessageType, SocketMessageConsumer> handlers;

    public DroneSocketHandler(MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;

        sessions = new ArrayList<>();
        handlers = new HashMap<>();
        initHandlers();
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

        SocketMessageConsumer consumer = handlers.get(mTemp.messageType);
        if (consumer != null) {
            consumer.consume(session, message.getPayload());
        } else {
            throw new Exception(String.format("no consumer for message with type '%s'", mTemp.messageType));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        this.sessions.remove(session);
    }

    private void initHandlers() {
        handlers.put(MessageType.SHOW_UP, this::onShowUp);
        handlers.put(MessageType.START_SESSION_ACK, this::onStartSessionAck);
    }

    private void onShowUp(WebSocketSession session, String payload) throws Exception {
        Message<DroneInfo> m = objectMapper.readValue(payload, new TypeReference<>() { });

        UnregisteredDrone info = new UnregisteredDrone();
        info.ip = m.payload.ip;
        info.position = m.payload.position;
        info.showUpTime = new Date();
        mongoTemplate.save(info);

        sendMessage(payload, MessageType.SHOW_UP);
    }

    private void onStartSessionAck(WebSocketSession session, String payload) throws Exception {
        Message<StartSessionAck> m = objectMapper.readValue(payload, new TypeReference<>() { });
        System.out.println(m.payload.successful);
    }
}
