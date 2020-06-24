package com.stakkato95.service.drone.domain.session.model.response;

import com.stakkato95.service.drone.model.common.Position;
import com.stakkato95.service.drone.model.session.Axis;
import com.stakkato95.service.drone.model.session.FlightState;
import com.stakkato95.service.drone.model.session.SessionState;

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
