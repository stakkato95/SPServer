package com.stakkato95.service.drone.socket;

import com.stakkato95.service.drone.domain.session.SessionRepository;
import com.stakkato95.service.drone.model.action.ActionType;
import com.stakkato95.service.drone.domain.action.ActionRepository;
import com.stakkato95.service.drone.domain.drone.DroneRepository;
import com.stakkato95.service.drone.model.drone.UnregisteredDrone;
import com.stakkato95.service.drone.socket.transport.MessageType;
import com.stakkato95.service.drone.socket.transport.model.request.*;
import com.stakkato95.service.drone.socket.transport.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DroneConnection implements SocketConnectionResponder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DroneConnection.class);

    private final DroneSocketHandler socketHandler;
    private final DroneRepository droneRepo;
    private final ActionRepository actionRepo;
    private final SessionRepository sessionRepo;

    private List<WebSocketSession> sessions = new ArrayList<>();

    public DroneConnection(DroneSocketHandler socketHandler,
                           DroneRepository droneRepo,
                           ActionRepository actionRepo,
                           SessionRepository sessionRepo) {
        this.socketHandler = socketHandler;
        this.socketHandler.setResponder(this);

        this.droneRepo = droneRepo;
        this.actionRepo = actionRepo;
        this.sessionRepo = sessionRepo;

        this.socketHandler.getEstablishedCon().subscribe(this::onSocketSessionStarted);
        this.socketHandler.getClosedCon().subscribe(this::onSocketSessionFinished);
    }

    public DroneSocketHandler getSocketHandler() {
        return socketHandler;
    }

    void onSocketSessionStarted(WebSocketSession session) {
        sessions.add(session);
    }

    void onSocketSessionFinished(WebSocketSession session) {
        sessions.remove(session);
    }

    @Override
    public void onShowUp(ShowUp showUp) {
        UnregisteredDrone unregisteredDrone = droneRepo.createUnregisteredDrone(showUp.ip, showUp.position);

        ShowUpAck ack = new ShowUpAck();
        ack.tempId = unregisteredDrone.id;
        send(showUp.ip, ack, MessageType.SHOW_UP_ACK);
    }

    @Override
    public void onRegistrationAck(RegistrationAck registrationAck) {
        droneRepo.confirmDroneRegistration(registrationAck.droneId);
    }

    @Override
    public void onStartSessionAck(StartSessionAck sessionAck) {
        sessionRepo.acknowledgeSessionStart(sessionAck.sessionId);
    }

    @Override
    public void onStopSessionAck(StopSessionAck ack) {
        sessionRepo.acknowledgeSessionStop(ack.sessionId);
    }

    @Override
    public void onPingAck(PingAck pingAck) {
        droneRepo.updateLastSeenTime(pingAck.droneId, pingAck.timestamp);
    }

    @Override
    public void onActionFinished(ActionFinished actionFinished) {
        actionRepo.finishAction(actionFinished.actionId);
    }

    public void sendPingToAll() {
        Ping ping = new Ping();
        ping.timestamp = new Date();

        for (WebSocketSession s : sessions) {
            String ip = s.getRemoteAddress().getAddress().toString();
            send(ip, ping, MessageType.PING);
        }
    }

    public void sendAction(String ip, String actionId, ActionType actionType, float value) {
        StartAction startAction = new StartAction();
        startAction.actionId = actionId;
        startAction.actionType = actionType;
        startAction.value = value;
        send(ip, startAction, MessageType.START_ACTION);
    }

    public void sendRegistration(String ip, String droneId) {
        Registration reg = new Registration();
        reg.id = droneId;
        send(ip, reg, MessageType.REGISTRATION);
    }

    public void sendStartSession(String ip, String sessionId) {
        StartSession startSession = new StartSession();
        startSession.sessionId = sessionId;
        send(ip, startSession, MessageType.START_SESSION);
    }

    public void sendStopSession(String ip, String sessionId) {
        StopSession stopSession = new StopSession();
        stopSession.sessionId = sessionId;
        send(ip, stopSession, MessageType.STOP_SESSION);
    }

    private <T> void send(String ip, T payload, MessageType messageType) {
        //TODO remove hardcode
        ip = "127.0.0.1";

        WebSocketSession session = getWebSocketSession(ip);
        if (session == null) {
            //TODO may be throw an exception
            LOGGER.error(String.format("Web socket connection for IP '%s' not found", ip));
            return;
        }

        try {
            socketHandler.sendMessage(session, payload, messageType);
        } catch (IOException e) {
            LOGGER.error(String.format("Error when sending %s", messageType), e);
        }
    }

    private WebSocketSession getWebSocketSession(String ip) {
        for (WebSocketSession s : sessions) {
            String sessionIp = s.getRemoteAddress().getAddress().getHostAddress();
            if (sessionIp.equals(ip)) {
                return s;
            }
        }

        return null;
    }
}
