package com.stakkato95.service.drone.transport;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum MessageType {
    SHOW_UP,
    SAVE_ID,
    START_SESSION,
    STOP_SESSION
}
