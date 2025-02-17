package com.stakkato95.service.drone.domain.action;

import com.stakkato95.service.drone.domain.session.SessionManager;
import com.stakkato95.service.drone.helper.BsonHelper;
import com.stakkato95.service.drone.helper.CommonHelper;
import com.stakkato95.service.drone.helper.Const;
import com.stakkato95.service.drone.helper.DatabaseUpdate;
import com.stakkato95.service.drone.model.action.Action;
import com.stakkato95.service.drone.model.session.Session;
import com.stakkato95.service.drone.domain.RestResponse;
import com.stakkato95.service.drone.domain.action.model.request.StartActionRequest;
import com.stakkato95.service.drone.socket.DroneConnection;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/action")
public class ActionRestController {

    private static final String DATABASE_NAME = "skynetz";
    private static final String COLLECTION = "action";

    private final MongoTemplate mongoTemplate;
    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final DroneConnection droneConnection;
    private final SessionManager sessionManager;

    public ActionRestController(MongoTemplate mongoTemplate,
                                DroneConnection droneConnection,
                                ReactiveMongoTemplate reactiveMongoTemplate,
                                SessionManager sessionManager) {
        this.mongoTemplate = mongoTemplate;
        this.droneConnection = droneConnection;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
        this.sessionManager = sessionManager;
    }

    @PostMapping(value = "/start", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<Action> start(@RequestBody StartActionRequest req) {
        Action action = sessionManager.sendAction(req.sessionId, req.actionType, req.value);

        RestResponse<Action> response = new RestResponse<>();
        response.successful = true;
        response.payload = action;
        return response;
    }

    @GetMapping(value = "/getAllRunning/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<List<Action>> getAllRunning(@PathVariable String sessionId) {
        boolean sessionExists = mongoTemplate.exists(
                Query.query(Criteria.where("id").is(sessionId)),
                Session.class
        );

        RestResponse<List<Action>> response = new RestResponse<>();

        if (!sessionExists) {
            response.successful = false;
            response.message = String.format("session with id '%s' doesn't exist", sessionId);
            return response;
        }

        List<Action> actions = mongoTemplate.find(
                Query.query(Criteria.where("sessionId").is(sessionId).and("actionState").is("RUNNING")),
                Action.class
        );

        response.successful = true;
        response.payload = actions;
        return response;
    }

    @GetMapping(value = "/getUpdates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<DatabaseUpdate<Action>> getUpdates() {
        return CommonHelper.getChangeStream(reactiveMongoTemplate, COLLECTION, Action.class);
    }

    @GetMapping(value = "/test")
    public String test() {
        return "hello";
    }
}
