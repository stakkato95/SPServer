package com.stakkato95.session.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "session")
public class Session {
    @Id
    public String id;
    public String droneId;
    public Date sessionStartTime;
    public Date sessionEndTime;
    public FlightState flightState;
    public Position position;
    public Axis rotation;
    public Axis speed;
}
