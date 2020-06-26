package com.stakkato95.service.drone.domain.telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stakkato95.service.drone.helper.Const;
import com.stakkato95.service.drone.model.telemetry.GNSS;
import com.stakkato95.service.drone.model.telemetry.Rotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;

import javax.swing.text.Position;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TelemetryManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelemetryManager.class);

    private static final String MQTT_RECEIVED_TOPIC = "mqtt_receivedTopic";

    private final ObjectMapper mapper;
    private final TelemetryRepository telemetryRepo;
    private final Map<String, Class<?>> topicToClass;

    public TelemetryManager(ObjectMapper mapper, TelemetryRepository telemetryRepo) {
        this.mapper = mapper;
        this.telemetryRepo = telemetryRepo;

        topicToClass = new HashMap<>();
        topicToClass.put(Const.MQTT_TOPIC_GNSS, GNSS.class);
        topicToClass.put(Const.MQTT_TOPIC_POSITION, Position.class);
        topicToClass.put(Const.MQTT_TOPIC_ROTATION, Rotation.class);
    }

    public void handleMqttMessage(Message<?> m) {
        String topic = m.getHeaders().get(MQTT_RECEIVED_TOPIC).toString();
        Class<?> modelClass = topicToClass.get(topic);

        try {
            telemetryRepo.save(mapper.readValue((String) m.getPayload(), modelClass));
        } catch (IOException e) {
            LOGGER.error(String.format("Error when parsing message from topic '%s'", topic), e);
        }
    }
}
