package com.stakkato95.service.drone.socket.transport;

public enum MessageType {
    SHOW_UP,
    REGISTRATION,

    START_SESSION,
    START_SESSION_ACK,

    PING,
    PING_ACK,

    STOP_SESSION
}
