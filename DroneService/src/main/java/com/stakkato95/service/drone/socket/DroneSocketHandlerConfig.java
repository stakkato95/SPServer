package com.stakkato95.service.drone.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class DroneSocketHandlerConfig {

    @Bean
    public DroneSocketHandler getDroneHandler(MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
        return new DroneSocketHandler(mongoTemplate, objectMapper);
    }
}
