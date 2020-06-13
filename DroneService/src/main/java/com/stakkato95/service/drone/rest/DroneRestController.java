package com.stakkato95.service.drone.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stakkato95.service.drone.model.Drone;
import com.stakkato95.service.drone.model.UnregisteredDrone;
import com.stakkato95.service.drone.rest.model.RegistrationRequest;
import com.stakkato95.service.drone.socket.DroneSocketHandler;
import com.stakkato95.service.drone.socket.transport.MessageType;
import com.stakkato95.service.drone.socket.transport.model.Registration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/drone")
public class DroneRestController {

    private final MongoTemplate mongoTemplate;
    private final DroneSocketHandler droneSocketHandler;
    private final ObjectMapper objectMapper;

    public DroneRestController(MongoTemplate mongoTemplate,
                               DroneSocketHandler droneSocketHandler,
                               ObjectMapper objectMapper) {
        this.mongoTemplate = mongoTemplate;
        this.droneSocketHandler = droneSocketHandler;
        this.objectMapper = objectMapper;
    }

    @GetMapping(value = "/getAllRegistered", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Drone> getAllRegistered() {
        return mongoTemplate.findAll(Drone.class);
    }

    @GetMapping(value = "/getAllUnregistered", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UnregisteredDrone> getAllUnregistered() {
        return mongoTemplate.findAll(UnregisteredDrone.class);
    }

    @PostMapping(value = "/registerNew", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<Drone> registerNew(@RequestBody RegistrationRequest registration) {
        RestResponse<Drone> response = new RestResponse<>();

        UnregisteredDrone unregistered = mongoTemplate.findById(registration.unregisteredId, UnregisteredDrone.class);
        if (unregistered == null) {
            response.successful = false;
            response.message = String.format("Drone with id '%s' not found", registration.unregisteredId);
            return response;
        }

        Drone drone = new Drone();
        drone.ip = unregistered.ip;
        drone.showUpTime = unregistered.showUpTime;
        drone.name = registration.name;
        drone.registrationTime = new Date();
        drone.lastConnectionTime = unregistered.showUpTime;
        drone = mongoTemplate.save(drone);

        try {
            Registration reg = new Registration();
            reg.id = drone.id;
            droneSocketHandler.sendMessage(reg, MessageType.REGISTRATION);
        } catch (Exception e) {
            response.successful = false;
            response.message = String.format("Exception: '%s'", e.getMessage());
            return response;
        }

        mongoTemplate.remove(
                Query.query(Criteria.where("id").is(registration.unregisteredId)),
                UnregisteredDrone.class
        );

        response.successful = true;
        response.payload = drone;
        return response;
    }
}
