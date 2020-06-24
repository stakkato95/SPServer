package com.stakkato95.service.drone.domain.drone;

import com.stakkato95.service.drone.model.drone.Drone;
import com.stakkato95.service.drone.model.drone.UnregisteredDrone;
import com.stakkato95.service.drone.domain.RestResponse;
import com.stakkato95.service.drone.domain.drone.model.RegistrationRequest;
import com.stakkato95.service.drone.socket.DroneConnection;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/drone")
public class DroneRestController {

    private final MongoTemplate mongoTemplate;
    private final DroneConnection droneConnection;

    public DroneRestController(MongoTemplate mongoTemplate, DroneConnection droneConnection) {
        this.mongoTemplate = mongoTemplate;
        this.droneConnection = droneConnection;
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
    public RestResponse<Drone> registerNew(@RequestBody RegistrationRequest registration) throws InterruptedException {
        Thread.sleep(2000);

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
        Date registrationTime = new Date();
        drone.registrationTime = registrationTime;
        drone.lastSeenTime = registrationTime;
        drone.lastConnectionTime = unregistered.showUpTime;
        drone = mongoTemplate.save(drone);

        droneConnection.sendRegistration(unregistered.ip, drone.id);

        //TODO uncomment
//        try {
//            Registration reg = new Registration();
//            reg.id = drone.id;
//            droneSocketHandler.sendMessage(reg, MessageType.REGISTRATION);
//        } catch (Exception e) {
//            response.successful = false;
//            response.message = String.format("Exception: '%s'", e.getMessage());
//            return response;
//        }

        mongoTemplate.remove(
                Query.query(Criteria.where("id").is(registration.unregisteredId)),
                UnregisteredDrone.class
        );

        response.successful = true;
        response.payload = drone;
        return response;
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<Drone> getDrone(@PathVariable String id) {
        Drone drone = mongoTemplate.findById(id, Drone.class);

        RestResponse<Drone> response = new RestResponse<>();
        if (drone == null) {
            response.successful = false;
            response.message = String.format("no drone with id '%s'", id);
            return response;
        }

        response.successful = true;
        response.payload = drone;
        return response;
    }
}
