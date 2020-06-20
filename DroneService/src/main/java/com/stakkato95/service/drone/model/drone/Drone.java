package com.stakkato95.service.drone.model.drone;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "drone")
public class Drone {
    @Id
    public String id;
    public String ip;
    public String name;
    public Date showUpTime;
    public Date registrationTime;
    public Date lastConnectionTime;
    public Date lastSeenTime;
}
