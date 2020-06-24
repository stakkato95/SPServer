package com.stakkato95.service.drone.socket;

import com.stakkato95.service.drone.socket.transport.model.response.ActionFinished;
import com.stakkato95.service.drone.socket.transport.model.response.ShowUp;
import com.stakkato95.service.drone.socket.transport.model.response.PingAck;
import com.stakkato95.service.drone.socket.transport.model.response.StartSessionAck;


public interface SocketConnectionResponder {

    void onShowUp(ShowUp showUp);

    void onStartSessionAck(StartSessionAck sessionAck);

    void onPingAck(PingAck pingAck);

    void onActionFinished(ActionFinished actionFinished);
}
