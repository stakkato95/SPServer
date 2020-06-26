package com.stakkato95.service.drone.model.session;

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
    public SessionState sessionState;
}
