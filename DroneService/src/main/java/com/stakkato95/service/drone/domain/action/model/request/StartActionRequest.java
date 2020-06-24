package com.stakkato95.service.drone.domain.action.model.request;

import com.stakkato95.service.drone.model.action.ActionType;

public class StartActionRequest {
    public String sessionId;
    public ActionType actionType;
    public float value;
}
