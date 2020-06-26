package com.stakkato95.service.drone.domain.telemetry;

import org.springframework.data.mongodb.core.MongoTemplate;

public class TelemetryRepository {

    private final MongoTemplate mongo;

    public TelemetryRepository(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    public <T> void save(T t) {
        mongo.save(t);
    }
}
