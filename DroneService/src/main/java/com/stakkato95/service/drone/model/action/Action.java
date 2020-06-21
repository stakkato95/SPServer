package com.stakkato95.service.drone.model.action;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "action")
public class Action {
    @Id
    public String id;
    public String sessionId;
    public ActionState actionState;
    public ActionType actionType;
    public float value;
}
