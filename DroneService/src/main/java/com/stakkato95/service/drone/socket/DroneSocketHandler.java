package com.stakkato95.service.drone.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stakkato95.service.drone.helper.PublishSubject;
import com.stakkato95.service.drone.socket.transport.Message;
import com.stakkato95.service.drone.socket.transport.MessageTemp;
import com.stakkato95.service.drone.socket.transport.MessageType;
import com.stakkato95.service.drone.socket.transport.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.publisher.EmitterProcessor;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class DroneSocketHandler extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DroneSocketHandler.class);

    private final ObjectMapper objectMapper;
    private final Map<MessageType, SocketMessageConsumer> handlers = new HashMap<>();
    private SocketConnectionResponder responder;

    final private PublishSubject<WebSocketSession> establishedCon  = new PublishSubject<>();
    final private PublishSubject<WebSocketSession> closedCon  = new PublishSubject<>();

    public DroneSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        initHandlers();
    }

    public EmitterProcessor<WebSocketSession> getEstablishedCon() {
        return establishedCon.getEmitter();
    }

    public EmitterProcessor<WebSocketSession> getClosedCon() {
        return closedCon.getEmitter();
    }

    void setResponder(SocketConnectionResponder responder) {
        this.responder = responder;
    }

    public <T> void sendMessage(WebSocketSession session, T payload, MessageType type) throws IOException {
        Message<T> message = new Message<>();
        message.messageType = type;
        message.payload = payload;

        String json = objectMapper.writeValueAsString(message);
        session.sendMessage(new TextMessage(json));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        establishedCon.next(session);
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
        closedCon.next(session);
    }

    private void initHandlers() {
        handlers.put(MessageType.SHOW_UP, (s, p) -> callResponder(p, responder::onShowUp, ShowUp.class));
        handlers.put(MessageType.START_SESSION_ACK, (s, p) -> callResponder(p, responder::onStartSessionAck, StartSessionAck.class));
        handlers.put(MessageType.PING_ACK, (s, p) -> callResponder(p, responder::onPingAck, PingAck.class));
        handlers.put(MessageType.ACTION_FINISHED, (s, p) -> callResponder(p, responder::onActionFinished, ActionFinished.class));
        handlers.put(MessageType.REGISTRATION_ACK, (s, p) -> callResponder(p, responder::onRegistrationAck, RegistrationAck.class));
        handlers.put(MessageType.STOP_SESSION_ACK, (s, p) -> callResponder(p, responder::onRegistrationAck, RegistrationAck.class));
    }

    private <T> void callResponder(String payload, Consumer<T> responderMethod, Class<T> contentClass) throws Exception {
        Message<T> m = objectMapper.readValue(
                payload,
                objectMapper.getTypeFactory().constructParametricType(Message.class, contentClass)
        );

        if (responder != null) {
            responderMethod.accept(m.payload);
        }
    }
}
