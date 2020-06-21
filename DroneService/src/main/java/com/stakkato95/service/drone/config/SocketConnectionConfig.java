package com.stakkato95.service.drone.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stakkato95.service.drone.socket.DroneConnection;
import com.stakkato95.service.drone.socket.DroneSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class SocketConnectionConfig {

    @Bean
    public DroneSocketHandler getDroneHandler(MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
        return new DroneSocketHandler(mongoTemplate, objectMapper);
    }

    @Bean
    public DroneConnection getDroneConnection(DroneSocketHandler handler, MongoTemplate mongo) {
        return new DroneConnection(handler, mongo);
    }
}
