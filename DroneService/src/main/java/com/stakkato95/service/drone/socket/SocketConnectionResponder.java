package com.stakkato95.service.drone.socket;

import com.stakkato95.service.drone.socket.transport.model.response.*;


public interface SocketConnectionResponder {

    void onShowUp(ShowUp showUp);

    void onRegistrationAck(RegistrationAck registrationAck);

    void onStartSessionAck(StartSessionAck sessionAck);

    void onPingAck(PingAck pingAck);

    void onActionFinished(ActionFinished actionFinished);
}
