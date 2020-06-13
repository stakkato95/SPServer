package com.stakkato95.service.drone.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "drone")
public class Drone {
    @Id
    public String id;
    public String lastIp;
    public String name;
    public Date showUpTime;
    public Date registrationTime;
    public Date lastConnectionTime;
}
