package com.stakkato95.service.drone.model.telemetry;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "telemetrySpeed")
public class Speed {
    @Id
    public String id;
    public float x;
    public float y;
    public float z;
}
