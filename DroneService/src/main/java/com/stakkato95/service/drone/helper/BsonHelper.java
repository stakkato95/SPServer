package com.stakkato95.service.drone.helper;

import org.springframework.data.mongodb.core.ChangeStreamEvent;

public final class BsonHelper {

    public static <T> String getObjectId(ChangeStreamEvent<T> e) {
        return e.getRaw().getDocumentKey().getObjectId("_id").getValue().toString();
    }
}
