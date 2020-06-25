package com.stakkato95.service.drone.domain.action;

import com.stakkato95.service.drone.model.action.Action;
import com.stakkato95.service.drone.model.action.ActionState;
import com.stakkato95.service.drone.model.action.ActionType;
import com.stakkato95.service.drone.model.drone.Drone;
import com.stakkato95.service.drone.model.session.Session;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public class ActionRepository {

    private final MongoTemplate mongo;

    public ActionRepository(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    public Action createAction(String sessionId, ActionType actionType, float value) {
        Action action = new Action();
        action.sessionId = sessionId;
        action.actionType = actionType;
        action.actionState = ActionState.RUNNING;
        action.value = value;
        return mongo.save(action);
    }

    public void finishAction(String actionId) {
        Action action = mongo.findById(actionId, Action.class);

        if (action == null) {
            return;
        }

        action.actionState = ActionState.FINISHED;
        mongo.save(action);
    }

    public void markRunningActionsAsInterrupted(String sessionId) {
        List<Action> actions = mongo.find(Query.query(Criteria.where("sessionId").is(sessionId)), Action.class);
        for (Action action : actions) {
            action.actionState = ActionState.INTERRUPTED;
            mongo.save(action);
        }
    }
}
