package com.stakkato95.service.drone.domain.session.model.response;

import com.stakkato95.service.drone.model.session.FlightState;
import com.stakkato95.service.drone.model.session.SessionState;

import java.util.Date;

public class StartSessionResponse {
    public String sessionId;
    public String droneId;
    public Date sessionStartTime;
    public SessionState sessionState;
    public FlightState flightState;
}
