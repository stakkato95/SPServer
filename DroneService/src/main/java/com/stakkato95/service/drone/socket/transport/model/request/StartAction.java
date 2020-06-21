package com.stakkato95.service.drone.socket.transport.model.request;

import com.stakkato95.service.drone.model.action.ActionType;

public class StartAction {
    public String actionId;
    public ActionType actionType;
    public float value;
}
