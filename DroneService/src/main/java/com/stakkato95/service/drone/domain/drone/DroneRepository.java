package com.stakkato95.service.drone.domain.drone;

import com.stakkato95.service.drone.model.common.Position;
import com.stakkato95.service.drone.model.drone.Drone;
import com.stakkato95.service.drone.model.drone.UnregisteredDrone;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Date;

public class DroneRepository {

    private final MongoTemplate mongo;

    public DroneRepository(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    public Drone getDroneById(String id) {
        return mongo.findById(id, Drone.class);
    }

    public void createUnregisteredDrone(String ip, Position position) {
        UnregisteredDrone info = new UnregisteredDrone();
        info.ip = ip;
        info.position = position;
        info.showUpTime = new Date();
        mongo.save(info);
    }

    public void updateLastSeenTime(String droneId, Date lastSeenTime) {
        Drone drone = mongo.findById(droneId, Drone.class);
        if (drone == null) {
            return;
        }

        drone.lastSeenTime = lastSeenTime;
        mongo.save(drone);
    }

    public Drone getDroneByIp(String ip) {
        return mongo.findOne(Query.query(Criteria.where("ip").is(ip)), Drone.class);
    }
}
