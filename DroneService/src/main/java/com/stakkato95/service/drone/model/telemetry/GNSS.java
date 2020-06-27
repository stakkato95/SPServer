package com.stakkato95.service.drone.model.telemetry;

import com.stakkato95.service.drone.helper.Const;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = Const.COLLECTION_GNSS)
public class GNSS {
    @Id
    public String id;
    public float lat;
    public float lon;
    public float alt;
    public Date timestamp;
}
