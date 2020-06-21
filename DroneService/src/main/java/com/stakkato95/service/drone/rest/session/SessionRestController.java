package com.stakkato95.service.drone.rest.session;

import com.stakkato95.service.drone.model.session.FlightState;
import com.stakkato95.service.drone.model.session.Session;
import com.stakkato95.service.drone.model.session.SessionState;
import com.stakkato95.service.drone.rest.RestResponse;
import com.stakkato95.service.drone.rest.session.model.request.SessionRequest;
import com.stakkato95.service.drone.rest.session.model.request.StartSessionRequest;
import com.stakkato95.service.drone.rest.session.model.request.StopSessionRequest;
import com.stakkato95.service.drone.rest.session.model.response.SessionResponse;
import com.stakkato95.service.drone.rest.session.model.response.StartSessionResponse;
import com.stakkato95.service.drone.socket.DroneSocketHandler;
import com.stakkato95.service.drone.socket.transport.MessageType;
import com.stakkato95.service.drone.socket.transport.model.request.StartSession;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/session")
public class SessionRestController {

    private final MongoTemplate mongoTemplate;
    private final DroneSocketHandler droneSocketHandler;

    public SessionRestController(MongoTemplate mongoTemplate, DroneSocketHandler droneSocketHandler) {
        this.mongoTemplate = mongoTemplate;
        this.droneSocketHandler = droneSocketHandler;
    }

    @PostMapping(value = "/startSession", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<StartSessionResponse> startSession(@RequestBody StartSessionRequest request) {
        Session session = new Session();
        session.droneId = request.droneId;
        session.flightState = FlightState.LANDED;
        session.sessionStartTime = new Date();
        session.sessionState = SessionState.RUNNING;
        session = mongoTemplate.save(session);

        try {
            StartSession startSession = new StartSession();
            startSession.sessionId = session.id;
            droneSocketHandler.sendMessage(startSession, MessageType.START_SESSION);
        } catch (IOException e) {
            RestResponse<StartSessionResponse> response = new RestResponse<>();
            response.successful = false;
            response.message = String.format("Exception: '%s'", e.getMessage());
            return response;
        }

        StartSessionResponse startSessionResponse = new StartSessionResponse();
        startSessionResponse.droneId = session.droneId;
        startSessionResponse.sessionId = session.id;
        startSessionResponse.sessionStartTime = session.sessionStartTime;
        startSessionResponse.sessionState = session.sessionState;
        startSessionResponse.flightState = session.flightState;

        RestResponse<StartSessionResponse> response = new RestResponse<>();
        response.successful = true;
        response.payload = startSessionResponse;
        return response;
    }

    @PostMapping(value = "/stopSession", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<SessionResponse> stopSession(@RequestBody StopSessionRequest request) {
        Session session = mongoTemplate.findById(request.sessionId, Session.class);

        RestResponse<SessionResponse> response = new RestResponse<>();

        if (session == null) {
            response.successful = false;
            response.message = String.format("no session with id '%s' found", request.sessionId);
            return response;
        }
        if (session.sessionState == SessionState.FINISHED) {
            response.successful = false;
            response.message = String.format("session with id '%s' is already finished", request.sessionId);
            return response;
        }

        session.sessionState = SessionState.FINISHED;
        session.sessionEndTime = new Date();
        mongoTemplate.save(session);

        response.successful = true;
        response.payload = populateSessionResponse(session);
        ;
        return response;
    }

    @GetMapping(value = "/getSession", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<SessionResponse> getSession(@RequestBody SessionRequest request) {
        Session session = mongoTemplate.findOne(
                Query.query(Criteria.where("droneId").is(request.droneId)),
                Session.class
        );

        RestResponse<SessionResponse> response = new RestResponse<>();

        if (session == null) {
            response.successful = false;
            response.message = String.format("no session with droneId '%s' found", request.droneId);
            return response;
        }

        response.successful = true;
        response.payload = populateSessionResponse(session);
        ;
        return response;
    }

    @GetMapping(value = "/getRunning", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<Session> getRunning() {
        Session session = mongoTemplate.findOne(
                Query.query(Criteria.where("sessionState").is("RUNNING")),
                Session.class
        );

        RestResponse<Session> response = new RestResponse<>();

        if (session == null) {
            response.successful = false;
            response.message = "no running sessions";
            return response;
        }

        response.successful = true;
        response.payload = session;
        return response;
    }

    private SessionResponse populateSessionResponse(Session session) {
        SessionResponse sessionResponse = new SessionResponse();
        sessionResponse.sessionId = session.id;
        sessionResponse.droneId = session.droneId;
        sessionResponse.flightState = session.flightState;
        sessionResponse.sessionState = session.sessionState;
        sessionResponse.sessionStartTime = session.sessionStartTime;
        sessionResponse.sessionEndTime = session.sessionEndTime;
        sessionResponse.position = session.position;
        sessionResponse.rotation = session.rotation;
        sessionResponse.speed = session.speed;
        return sessionResponse;
    }
}
