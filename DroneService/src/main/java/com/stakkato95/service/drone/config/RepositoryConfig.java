package com.stakkato95.service.drone.config;

import com.stakkato95.service.drone.domain.action.ActionRepository;
import com.stakkato95.service.drone.domain.drone.DroneRepository;
import com.stakkato95.service.drone.domain.session.SessionRepository;
import com.stakkato95.service.drone.domain.telemetry.TelemetryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class RepositoryConfig {
    @Bean
    public SessionRepository getSessionRepo(MongoTemplate template, DroneRepository droneRepo) {
        return new SessionRepository(template, droneRepo);
    }

    @Bean
    public DroneRepository getDroneRepo(MongoTemplate template) {
        return new DroneRepository(template);
    }

    @Bean
    public ActionRepository getActionRepo(MongoTemplate template) {
        return new ActionRepository(template);
    }

    @Bean
    public TelemetryRepository getTelemetryRepo(MongoTemplate template) {
        return new TelemetryRepository(template);
    }
}
