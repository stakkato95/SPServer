package com.stakkato95.service.drone.domain.session;

import com.stakkato95.service.drone.helper.BsonHelper;
import com.stakkato95.service.drone.helper.DatabaseUpdate;
import com.stakkato95.service.drone.model.action.Action;
import com.stakkato95.service.drone.model.session.Session;
import com.stakkato95.service.drone.model.session.SessionState;
import com.stakkato95.service.drone.domain.RestResponse;
import com.stakkato95.service.drone.domain.session.model.request.SessionRequest;
import com.stakkato95.service.drone.domain.session.model.request.StartSessionRequest;
import com.stakkato95.service.drone.domain.session.model.request.StopSessionRequest;
import com.stakkato95.service.drone.domain.session.model.response.SessionResponse;
import com.stakkato95.service.drone.domain.session.model.response.StartSessionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Date;

@CrossOrigin
@RestController
@RequestMapping("/api/session")
public class SessionRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionRestController.class);

    private static final String DATABASE_NAME = "skynetz";
    private static final String COLLECTION_TO_LISTEN = "session";

    private final MongoTemplate mongoTemplate;
    private final ReactiveMongoTemplate reactiveMongo;
    private final SessionManager sessionManager;

    public SessionRestController(MongoTemplate mongoTemplate,
                                 ReactiveMongoTemplate reactiveMongo,
                                 SessionManager sessionManager) {
        this.mongoTemplate = mongoTemplate;
        this.reactiveMongo = reactiveMongo;
        this.sessionManager = sessionManager;
    }

    @PostMapping(value = "/startSession", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<StartSessionResponse> startSession(@RequestBody(required = false) StartSessionRequest request) {
        Session session = sessionManager.startSession(request.droneId);

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

    @GetMapping(value = "/getUpdates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<DatabaseUpdate<Session>> getUpdates() {
        LOGGER.error("CALLED");
        return reactiveMongo.changeStream(
                DATABASE_NAME,
                COLLECTION_TO_LISTEN,
                ChangeStreamOptions.builder().build(),
                Session.class
        ).map(e -> {
            DatabaseUpdate<Session> update = new DatabaseUpdate<>();
            update.id = BsonHelper.getObjectId(e);
            update.object = e.getBody();
            return update;
        });
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
