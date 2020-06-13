package com.stakkato95.service.drone.rest;

import com.stakkato95.service.drone.model.Drone;
import com.stakkato95.service.drone.model.UnregisteredDrone;
import com.stakkato95.service.drone.rest.model.RegistrationRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
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

    @PostMapping(value = "/registerNew", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<Drone> registerNew(@RequestBody RegistrationRequest registration) {
        UnregisteredDrone unregistered = mongoTemplate.findById(registration.unregisteredId, UnregisteredDrone.class);
        if (unregistered == null) {
            RestResponse<Drone> response = new RestResponse<>();
            response.successful = false;
            return response;
        }

        Drone drone = new Drone();
        drone.ip = unregistered.ip;
        drone.showUpTime = unregistered.showUpTime;
        drone.name = registration.name;
        drone.registrationTime = new Date();
        drone.lastConnectionTime = unregistered.showUpTime;

        drone = mongoTemplate.save(drone);

        RestResponse<Drone> response = new RestResponse<>();
        response.successful = false;
        response.payload = drone;
        return response;
    }
}
