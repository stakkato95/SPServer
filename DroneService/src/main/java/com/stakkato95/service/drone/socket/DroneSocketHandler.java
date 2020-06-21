package com.stakkato95.service.drone.socket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stakkato95.service.drone.socket.transport.Message;
import com.stakkato95.service.drone.socket.transport.MessageTemp;
import com.stakkato95.service.drone.socket.transport.MessageType;
import com.stakkato95.service.drone.socket.transport.model.response.DroneInfo;
import com.stakkato95.service.drone.socket.transport.model.response.PingAck;
import com.stakkato95.service.drone.socket.transport.model.response.StartSessionAck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class DroneSocketHandler extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DroneSocketHandler.class);

    private final ObjectMapper objectMapper;
    private final MongoTemplate mongoTemplate;
    private List<WebSocketSession> sessions;
    private Map<MessageType, SocketMessageConsumer> handlers;
    private SocketConnectionResponder connectionResponder;

    public DroneSocketHandler(MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;

        sessions = new ArrayList<>();
        handlers = new HashMap<>();
        initHandlers();
    }

    void setConnectionResponder(SocketConnectionResponder connectionResponder) {
        this.connectionResponder = connectionResponder;
    }

    public <T> void sendMessage(T payload, MessageType type) throws IOException {
        if (sessions.isEmpty()) {
            LOGGER.error("Message can't be sent. Drone isn't connected.");
            return;
        }

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
        handlers.put(MessageType.PING_ACK, this::onPingAck);
    }

    private void onShowUp(WebSocketSession session, String payload) throws Exception {
        callResponder(payload, connectionResponder::onShowUp, DroneInfo.class);
    }

    private void onStartSessionAck(WebSocketSession session, String payload) throws Exception {
        callResponder(payload, connectionResponder::onStartSessionAck, StartSessionAck.class);
    }

    private void onPingAck(WebSocketSession session, String payload) throws Exception {
        callResponder(payload, connectionResponder::onPingAck, PingAck.class);
    }

    private <T> void callResponder(String payload, Consumer<T> responderMethod, Class<T> contentClass) throws Exception {
        Message<T> m = objectMapper.readValue(
                payload,
                objectMapper.getTypeFactory().constructParametricType(Message.class, contentClass)
        );

        if (connectionResponder != null) {
            responderMethod.accept(m.payload);
        }
    }
}
