package com.stakkato95.service.drone.model.telemetry;

import com.stakkato95.service.drone.helper.Const;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = Const.COLLECTION_SPEED)
public class Speed {
    @Id
    public String id;
    public float x;
    public float y;
    public float z;
}
