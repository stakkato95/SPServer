package com.stakkato95.service.drone.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "unregisteredDrone")
public class UnregisteredDrone {
    @Id
    public String id;
    public String ip;
    public Position position;
}
