package com.stakkato95.service.drone.domain.session;

import com.stakkato95.service.drone.domain.drone.DroneRepository;
import com.stakkato95.service.drone.model.drone.Drone;
import com.stakkato95.service.drone.model.session.FlightState;
import com.stakkato95.service.drone.model.session.Session;
import com.stakkato95.service.drone.model.session.SessionState;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Date;

public class SessionRepository {

    private final MongoTemplate mongo;
    private final DroneRepository droneRepo;

    public SessionRepository(MongoTemplate mongo, DroneRepository droneRepo) {
        this.mongo = mongo;
        this.droneRepo = droneRepo;
    }

    public Session droneSessionExists(String ip) {
        Drone drone = droneRepo.getDroneByIp(ip);
        if (drone == null) {
            return null;
        }

        return mongo.findOne(Query.query(Criteria.where("droneId").is(drone.id)), Session.class);
    }

    public void markSessionAsInterrupted(Session session) {
        session.sessionState = SessionState.INTERRUPTED;
        mongo.save(session);
    }

    public Session getRunningSession() {
        return mongo.findOne(
                Query.query(Criteria.where("sessionState").is(SessionState.RUNNING.toString())),
                Session.class
        );
    }

    public Session createNewSession(String droneId) {
        Session session = new Session();
        session.droneId = droneId;
        session.flightState = FlightState.LANDED;
        session.sessionStartTime = new Date();
        session.sessionState = SessionState.RUNNING;
        session.sessionStartAcknowledged = false;
        session.sessionStopAcknowledged = false;
        return mongo.save(session);
    }

    public Session stopSession(String sessionId) {
        Session session = getSessionById(sessionId);
        if (session == null) {
            return null;
        }

        session.sessionState = SessionState.FINISHED;
        session.sessionEndTime = new Date();
        return mongo.save(session);
    }

    public Session getSessionById(String id) {
        return mongo.findById(id, Session.class);
    }

    public Session acknowledgeSessionStart(String sessionId) {
        Session session = getSessionById(sessionId);
        if (session == null) {
            return null;
        }

        session.sessionStartAcknowledged = true;
        return mongo.save(session);
    }

    public Session acknowledgeSessionStop(String sessionId) {
        Session session = getSessionById(sessionId);
        if (session == null) {
            return null;
        }

        session.sessionStopAcknowledged = true;
        return mongo.save(session);
    }
}
