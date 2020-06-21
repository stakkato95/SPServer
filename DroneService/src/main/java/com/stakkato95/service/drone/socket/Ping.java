package com.stakkato95.service.drone.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class Ping {

    private static final Logger log = LoggerFactory.getLogger(Ping.class);

    private final DroneSocketHandler socketHandler;

    public Ping(DroneSocketHandler socketHandler) {
        this.socketHandler = socketHandler;
    }

    @Scheduled(fixedRate = 1000)
    public void sendPing() {
        log.info(new Date().toString());
    }
}
