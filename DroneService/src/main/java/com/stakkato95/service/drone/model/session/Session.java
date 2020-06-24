package com.stakkato95.service.drone.model.session;

import com.stakkato95.service.drone.model.common.Position;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "session")
public class Session {
    @Id
    public String id;
    public String socketSessionId;
    public String droneId;
    public Date sessionStartTime;
    public Date sessionEndTime;
    public Position position;
    public Axis rotation;
    public Axis speed;
    public FlightState flightState;
    public SessionState sessionState;
}
