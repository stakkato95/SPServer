package com.stakkato95.service.drone.socket.transport;

public enum MessageType {
    SHOW_UP,
    SHOW_UP_ACK,

    REGISTRATION,
    REGISTRATION_ACK,

    START_SESSION,
    START_SESSION_ACK,

    PING,
    PING_ACK,

    START_ACTION,
    START_ACTION_ACK, //TODO future release
    ACTION_FINISHED,

    STOP_SESSION,
    STOP_SESSION_ACK
}
