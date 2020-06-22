package com.stakkato95.service.drone.rest.action;

import com.stakkato95.service.drone.model.action.Action;
import com.stakkato95.service.drone.model.action.ActionState;
import com.stakkato95.service.drone.model.session.Session;
import com.stakkato95.service.drone.rest.RestResponse;
import com.stakkato95.service.drone.rest.action.model.request.AllActionsRequest;
import com.stakkato95.service.drone.rest.action.model.request.StartActionRequest;
import com.stakkato95.service.drone.socket.DroneConnection;
import org.reactivestreams.Publisher;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Function;

@CrossOrigin
@RestController
@RequestMapping("/api/action")
public class ActionRestController {

    private final MongoTemplate mongoTemplate;
    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final DroneConnection droneConnection;

    public ActionRestController(MongoTemplate mongoTemplate,
                                DroneConnection droneConnection,
                                ReactiveMongoTemplate reactiveMongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.droneConnection = droneConnection;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @PostMapping(value = "/start", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<Action> start(@RequestBody StartActionRequest request) {
        Session session = mongoTemplate.findById(request.sessionId, Session.class);

        RestResponse<Action> response = new RestResponse<>();

        if (session == null) {
            response.successful = false;
            response.message = String.format("session with id '%s' doesn't exist", request.sessionId);
            return response;
        }

        Action action = new Action();
        action.sessionId = request.sessionId;
        action.actionType = request.actionType;
        action.actionState = ActionState.RUNNING;
        action.value = request.value;

        action = mongoTemplate.save(action);

        //TODO implement logic with acknowledgements
        droneConnection.sendAction(action.id, request.actionType, request.value);

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
                Query.query(Criteria.where("sessionId").is(sessionId)),
                Action.class
        );

        response.successful = true;
        response.payload = actions;
        return response;
    }

    @GetMapping(value = "/getUpdates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Action> getUpdates() {
        return reactiveMongoTemplate.changeStream(Action.class).listen().map(ChangeStreamEvent::getBody);
    }

    @GetMapping(value = "/test")
    public String test() {
        return "hello";
    }
}
