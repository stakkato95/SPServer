package com.stakkato95.service.drone.socket;

import com.stakkato95.service.drone.model.action.ActionType;
import com.stakkato95.service.drone.model.drone.Drone;
import com.stakkato95.service.drone.model.drone.UnregisteredDrone;
import com.stakkato95.service.drone.socket.transport.MessageType;
import com.stakkato95.service.drone.socket.transport.model.request.Ping;
import com.stakkato95.service.drone.socket.transport.model.response.DroneInfo;
import com.stakkato95.service.drone.socket.transport.model.response.PingAck;
import com.stakkato95.service.drone.socket.transport.model.response.StartSessionAck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.util.Date;

public class DroneConnection implements SocketConnectionResponder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DroneConnection.class);

    private final DroneSocketHandler socketHandler;
    private final MongoTemplate mongoTemplate;

    public DroneConnection(DroneSocketHandler socketHandler, MongoTemplate mongoTemplate) {
        this.socketHandler = socketHandler;
        this.socketHandler.setConnectionResponder(this);

        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void onShowUp(DroneInfo droneInfo) {
        UnregisteredDrone info = new UnregisteredDrone();
        info.ip = droneInfo.ip;
        info.position = droneInfo.position;
        info.showUpTime = new Date();
        mongoTemplate.save(info);
    }

    @Override
    public void onStartSessionAck(StartSessionAck sessionAck) {
        LOGGER.info("StartSessionAck %b", sessionAck.successful);
    }

    @Override
    public void onPingAck(PingAck pingAck) {
        Drone drone = mongoTemplate.findById(pingAck.droneId, Drone.class);
        if (drone == null) {
            LOGGER.error("drone with id '%s' is not found", pingAck.droneId);
            return;
        }

        drone.lastSeenTime = pingAck.timestamp;
        mongoTemplate.save(drone);
    }

    public void sendPing() {
        Ping ping = new Ping();
        ping.timestamp = new Date();

        try {
            socketHandler.sendMessage(ping, MessageType.PING);
        } catch (IOException e) {
            LOGGER.error("Error when sending ping", e);
        }
    }

    public void sendAction(ActionType actionType, float value) {
        //TODO
    }
}
