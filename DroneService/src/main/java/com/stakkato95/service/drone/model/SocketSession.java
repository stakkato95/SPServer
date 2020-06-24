package com.stakkato95.service.drone.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "socketSession")
public class SocketSession {
    @Id
    public String id;
    public String webSocketSessionId;
    public String droneId;
}
