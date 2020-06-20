package com.stakkato95.session.rest.model.response;

import com.stakkato95.session.model.Axis;
import com.stakkato95.session.model.FlightState;
import com.stakkato95.session.model.Position;
import com.stakkato95.session.model.SessionState;

import java.util.Date;

public class SessionResponse {
    public String sessionId;
    public String droneId;
    public Date sessionStartTime;
    public Date sessionEndTime;
    public FlightState flightState;
    public SessionState sessionState;
    public Position position;
    public Axis rotation;
    public Axis speed;
}
