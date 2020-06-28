package com.stakkato95.service.drone.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stakkato95.service.drone.domain.action.ActionRepository;
import com.stakkato95.service.drone.domain.drone.DroneRepository;
import com.stakkato95.service.drone.domain.session.SessionRepository;
import com.stakkato95.service.drone.domain.session.SessionManager;
import com.stakkato95.service.drone.socket.DroneConnection;
import com.stakkato95.service.drone.socket.DroneSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocketConnectionConfig {

    @Bean
    public DroneSocketHandler getDroneHandler(ObjectMapper objectMapper) {
        return new DroneSocketHandler(objectMapper);
    }

    @Bean
    public DroneConnection getDroneConnection(DroneSocketHandler handler,
                                              DroneRepository droneRepo,
                                              ActionRepository actionRepo,
                                              SessionRepository sessionRepo) {
        return new DroneConnection(handler, droneRepo, actionRepo, sessionRepo);
    }

    @Bean
    public SessionManager getSessionManager(DroneConnection con,
                                            SessionRepository sessionRepo,
                                            DroneRepository droneRepo,
                                            ActionRepository actionRepo) {
        return new SessionManager(con, sessionRepo, droneRepo, actionRepo);
    }
}
