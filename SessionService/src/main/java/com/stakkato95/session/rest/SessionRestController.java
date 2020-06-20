package com.stakkato95.session.rest;

import com.stakkato95.session.model.FlightState;
import com.stakkato95.session.model.Session;
import com.stakkato95.session.model.SessionState;
import com.stakkato95.session.rest.model.request.StartSessionRequest;
import com.stakkato95.session.rest.model.request.SessionRequest;
import com.stakkato95.session.rest.model.request.StopSessionRequest;
import com.stakkato95.session.rest.model.response.StartSessionResponse;
import com.stakkato95.session.rest.model.response.SessionResponse;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/session")
public class SessionRestController {

    private final MongoTemplate mongoTemplate;

    public SessionRestController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostMapping(value = "/startSession", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<StartSessionResponse> startSession(@RequestBody StartSessionRequest request) {
        Session session = new Session();
        session.droneId = request.droneId;
        session.flightState = FlightState.LANDED;
        session.sessionStartTime = new Date();
        session.sessionState = SessionState.RUNNING;
        session = mongoTemplate.save(session);

        StartSessionResponse startSessionResponse = new StartSessionResponse();
        startSessionResponse.droneId = session.droneId;
        startSessionResponse.sessionId = session.id;
        startSessionResponse.sessionStartTime = session.sessionStartTime;

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
        response.payload = populateSessionResponse(session);;
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
        response.payload = populateSessionResponse(session);;
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
