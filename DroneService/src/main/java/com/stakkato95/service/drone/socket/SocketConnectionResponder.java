package com.stakkato95.service.drone.socket;

import com.stakkato95.service.drone.socket.transport.model.response.ActionFinished;
import com.stakkato95.service.drone.socket.transport.model.response.DroneInfo;
import com.stakkato95.service.drone.socket.transport.model.response.PingAck;
import com.stakkato95.service.drone.socket.transport.model.response.StartSessionAck;


public interface SocketConnectionResponder {

    void onShowUp(DroneInfo droneInfo);

    void onStartSessionAck(StartSessionAck sessionAck);

    void onPingAck(PingAck pingAck);

    void onActionFinished(ActionFinished actionFinished);
}
