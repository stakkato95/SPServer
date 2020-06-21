package com.stakkato95.service.drone.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PingScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingScheduler.class);

    private final DroneConnection connection;

    public PingScheduler(DroneConnection connection) {
        this.connection = connection;
    }

    @Scheduled(fixedRateString = "${socket.ping-delay-millisec}")
    public void sendPing() {
        connection.sendPing();
    }
}
