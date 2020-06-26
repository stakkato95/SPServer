package com.stakkato95.service.drone.helper;

public interface Const {
    String DB_NAME = "skynetz";

    String MQTT_TOPIC_GNSS = "drone/telemetry/gnss";
    String MQTT_TOPIC_POSITION = "drone/telemetry/position";
    String MQTT_TOPIC_ROTATION = "drone/telemetry/rotation";
}
