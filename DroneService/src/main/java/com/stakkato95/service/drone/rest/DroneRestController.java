package com.stakkato95.service.drone.rest;

import com.stakkato95.service.drone.model.Drone;
import com.stakkato95.service.drone.model.UnregisteredDrone;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/drone")
public class DroneRestController {

    private final MongoTemplate mongoTemplate;

    public DroneRestController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping(value = "/getAllRegistered", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Drone> getAllRegistered() {
        return mongoTemplate.findAll(Drone.class);
    }

    @GetMapping(value = "/getAllUnregistered", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UnregisteredDrone> getAllUnregistered() {
        return mongoTemplate.findAll(UnregisteredDrone.class);
    }
}
