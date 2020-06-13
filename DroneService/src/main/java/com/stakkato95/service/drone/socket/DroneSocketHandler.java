package com.stakkato95.service.drone.socket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stakkato95.service.drone.transport.model.DroneInfo;
import com.stakkato95.service.drone.model.UnregisteredDrone;
import com.stakkato95.service.drone.transport.Message;
import com.stakkato95.service.drone.transport.MessageTemp;
import com.stakkato95.service.drone.transport.MessageType;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DroneSocketHandler extends TextWebSocketHandler {

    private List<WebSocketSession> sessions;
    private final ObjectMapper objectMapper;
    private final MongoTemplate mongoTemplate;

    public DroneSocketHandler(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;

        sessions = new ArrayList<>();
        objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY, false)
                .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                .configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, false)
                .configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, false)
                .configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, false);
    }

    public void sendMessage(String message) throws IOException {
        sessions.get(0).sendMessage(new TextMessage(message));
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
            mongoTemplate.save(info);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        this.sessions.remove(session);
    }
}
