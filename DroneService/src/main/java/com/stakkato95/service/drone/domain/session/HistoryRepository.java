package com.stakkato95.service.drone.domain.session;

import com.stakkato95.service.drone.domain.action.ActionRepository;
import com.stakkato95.service.drone.domain.drone.DroneRepository;
import com.stakkato95.service.drone.domain.session.model.History;
import com.stakkato95.service.drone.model.drone.Drone;
import com.stakkato95.service.drone.model.session.Session;

import java.util.ArrayList;
import java.util.List;

public class HistoryRepository {

    private final SessionRepository sessionRepo;
    private final DroneRepository droneRepo;
    private final ActionRepository actionRepo;

    public HistoryRepository(SessionRepository sessionRepo,
                             DroneRepository droneRepo,
                             ActionRepository actionRepo) {
        this.sessionRepo = sessionRepo;
        this.droneRepo = droneRepo;
        this.actionRepo = actionRepo;
    }

    public List<History> getAllHistory() {
        List<Session> sessions = sessionRepo.getAllSessions();

        List<History> historyList = new ArrayList<>();
        for (Session s : sessions) {
            Drone drone = droneRepo.getDroneById(s.droneId);
            int actions = actionRepo.getAllActions(s.id).size();

            History history = new History();
            history.session = s;
            history.drone = drone;
            history.actions = actions;
            historyList.add(history);
        }

        return historyList;
    }
}
