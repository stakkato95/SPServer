package com.stakkato95.service.drone.domain.session;

import com.stakkato95.service.drone.domain.action.ActionRepository;
import com.stakkato95.service.drone.domain.drone.DroneRepository;
import com.stakkato95.service.drone.model.action.Action;
import com.stakkato95.service.drone.model.action.ActionType;
import com.stakkato95.service.drone.model.drone.Drone;
import com.stakkato95.service.drone.model.session.Session;
import com.stakkato95.service.drone.model.session.SessionState;
import com.stakkato95.service.drone.socket.DroneConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SessionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionManager.class);

    private final DroneConnection droneConnection;
    private final SessionRepository sessionRepo;
    private final DroneRepository droneRepo;
    private final ActionRepository actionRepo;

    private final Map<String, String> sessionIdToDroneIp = new HashMap<>();

    public SessionManager(DroneConnection droneConnection,
                          SessionRepository sessionRepo,
                          DroneRepository droneRepo,
                          ActionRepository actionRepo) {
        this.droneConnection = droneConnection;
        this.sessionRepo = sessionRepo;
        this.droneRepo = droneRepo;
        this.actionRepo = actionRepo;

        this.droneConnection.getSocketHandler()
                .getEstablishedCon()
                .map(WebSocketSession::getRemoteAddress)
                .map(InetSocketAddress::getAddress)
                .map(InetAddress::getHostAddress)
                .subscribe(this::onSocketSessionStarted);

        this.droneConnection.getSocketHandler()
                .getClosedCon()
                .map(WebSocketSession::getRemoteAddress)
                .map(InetSocketAddress::getAddress)
                .map(InetAddress::getHostAddress)
                .subscribe(this::onSocketSessionFinished);
    }

    void onSocketSessionStarted(String ip) {
        //TODO etwas??????
    }

    void onSocketSessionFinished(String ip) {
        //TODO replace
        ip = "192.168.1.101";

        Session session = sessionRepo.droneSessionExists(ip);
        if (session == null) {
            return;
        }

        sessionRepo.markSessionAsInterrupted(session);
        actionRepo.markRunningActionsAsInterrupted(session.id);
    }

    public Session startSession(String droneId) {
        Session session = sessionRepo.createNewSession(droneId);
        Drone drone = droneRepo.getDroneById(droneId);
        droneConnection.sendStartSession(drone.ip, session.id);
        sessionIdToDroneIp.put(session.id, drone.ip);
        return session;
    }

    public Session stopSession(String sessionId) {
        return sessionRepo.stopSession(sessionId);
    }

    public Action sendAction(String sessionId, ActionType actionType, float value) {
        Action action = actionRepo.createAction(sessionId, actionType, value);
        droneConnection.sendAction(sessionIdToDroneIp.get(sessionId), action.id, actionType, value);
        return action;
    }
}
