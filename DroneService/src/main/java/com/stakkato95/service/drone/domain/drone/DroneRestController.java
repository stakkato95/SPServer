package com.stakkato95.service.drone.domain.drone;

import com.stakkato95.service.drone.helper.CommonHelper;
import com.stakkato95.service.drone.helper.DatabaseUpdate;
import com.stakkato95.service.drone.model.drone.Drone;
import com.stakkato95.service.drone.model.drone.UnregisteredDrone;
import com.stakkato95.service.drone.domain.RestResponse;
import com.stakkato95.service.drone.domain.drone.model.RegistrationRequest;
import com.stakkato95.service.drone.socket.DroneConnection;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/drone")
public class DroneRestController {

    private static final String COLLECTION = "drone";

    private final DroneRepository droneRepo;
    private final DroneConnection droneConnection;
    private final ReactiveMongoTemplate reactiveMongo;

    public DroneRestController(DroneConnection droneConnection,
                               DroneRepository droneRepo,
                               ReactiveMongoTemplate reactiveMongo) {
        this.droneRepo = droneRepo;
        this.droneConnection = droneConnection;
        this.reactiveMongo = reactiveMongo;
    }

    @GetMapping(value = "/getAllRegistered", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Drone> getAllRegistered() {
        return droneRepo.getAllRegisteredDrones();
    }

    @GetMapping(value = "/getAllUnregistered", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UnregisteredDrone> getAllUnregistered() {
        return droneRepo.getAllUnregisteredDrones();
    }

    @PostMapping(value = "/registerNew", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<Drone> registerNew(@RequestBody RegistrationRequest registration) throws InterruptedException {
        Thread.sleep(2000);
        UnregisteredDrone unregistered = droneRepo.getUnregisteredDroneById(registration.unregisteredId);

        RestResponse<Drone> response = new RestResponse<>();
        if (unregistered == null) {
            response.successful = false;
            response.message = String.format("Drone with id '%s' not found", registration.unregisteredId);
            return response;
        }

        Drone drone = droneRepo.createDrone(unregistered.ip, unregistered.showUpTime, registration.name);
        droneConnection.sendRegistration(unregistered.ip, drone.id);
        droneRepo.removeUnregisteredDrone(registration.unregisteredId);

        response.successful = true;
        response.payload = drone;
        return response;
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<Drone> getDrone(@PathVariable String id) {
        Drone drone = droneRepo.getDroneById(id);

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

    @GetMapping(value = "/getUpdates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<DatabaseUpdate<Drone>> getUpdates() {
        return CommonHelper.getChangeStream(reactiveMongo, COLLECTION, Drone.class);
    }
}
