package com.stakkato95.service.drone.domain.drone;

import com.stakkato95.service.drone.model.drone.Position;
import com.stakkato95.service.drone.model.drone.Drone;
import com.stakkato95.service.drone.model.drone.UnregisteredDrone;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Date;
import java.util.List;

public class DroneRepository {

    private final MongoTemplate mongo;

    public DroneRepository(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    public Drone getDroneById(String id) {
        return mongo.findById(id, Drone.class);
    }

    public UnregisteredDrone createUnregisteredDrone(String ip, Position position) {
        UnregisteredDrone info = new UnregisteredDrone();
        info.ip = ip;
        info.position = position;
        info.showUpTime = new Date();
        return mongo.save(info);
    }

    public void updateLastSeenTime(String id, Date lastSeenTime) {
        Drone drone = getDroneById(id);
        if (drone != null) {
            drone.lastSeenTime = lastSeenTime;
            mongo.save(drone);
            return;
        }

        UnregisteredDrone unregisteredDrone = mongo.findById(id, UnregisteredDrone.class);
        if (unregisteredDrone != null) {
            unregisteredDrone.lastSeenTime = lastSeenTime;
            mongo.save(unregisteredDrone);
        }
    }

    public Drone getDroneByIp(String ip) {
        return mongo.findOne(Query.query(Criteria.where("ip").is(ip)), Drone.class);
    }

    public List<Drone> getAllRegisteredDrones() {
        return mongo.findAll(Drone.class);
    }

    public List<UnregisteredDrone> getAllUnregisteredDrones() {
        return mongo.findAll(UnregisteredDrone.class);
    }

    public UnregisteredDrone getUnregisteredDroneById(String id) {
        return mongo.findById(id, UnregisteredDrone.class);
    }

    public Drone createDrone(String ip, Date showUpTime, String name) {
        Drone drone = new Drone();
        drone.ip = ip;
        drone.showUpTime = showUpTime;
        drone.name = name;
        Date registrationTime = new Date();
        drone.registrationTime = registrationTime;
        drone.lastSeenTime = registrationTime;
        drone.lastConnectionTime = showUpTime;
        drone.registrationConfirmed = false;
        return mongo.save(drone);
    }

    public void removeUnregisteredDrone(String id) {
        mongo.remove(Query.query(Criteria.where("id").is(id)), UnregisteredDrone.class);
    }

    public void confirmDroneRegistration(String droneId) {
        Drone drone = getDroneById(droneId);
        if (drone == null) {
            return;
        }

        drone.registrationConfirmed = true;
        mongo.save(drone);
    }
}
