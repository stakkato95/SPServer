package com.stakkato95.service.drone.model.telemetry;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "telemetryGnss")
public class GNSS {
    @Id
    public String id;
    public float lat;
    public float lon;
    public float alt;
}
