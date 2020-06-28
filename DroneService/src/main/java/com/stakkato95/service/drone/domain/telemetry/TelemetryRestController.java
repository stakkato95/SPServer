package com.stakkato95.service.drone.domain.telemetry;

import com.stakkato95.service.drone.helper.CommonHelper;
import com.stakkato95.service.drone.helper.Const;
import com.stakkato95.service.drone.helper.DatabaseUpdate;
import com.stakkato95.service.drone.helper.DatabaseUpdateNew;
import com.stakkato95.service.drone.model.telemetry.GNSS;
import com.stakkato95.service.drone.model.telemetry.Rotation;
import com.stakkato95.service.drone.model.telemetry.Speed;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@CrossOrigin
@RestController
@RequestMapping("/api/telemetry")
public class TelemetryRestController {

    private final ReactiveMongoTemplate reactiveMongo;

    public TelemetryRestController(ReactiveMongoTemplate reactiveMongo) {
        this.reactiveMongo = reactiveMongo;
    }

    @GetMapping(value = "/gnss/getUpdates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<DatabaseUpdate<GNSS>> getGnssUpdates() {
        return CommonHelper.getChangeStream(reactiveMongo, Const.COLLECTION_GNSS, GNSS.class);
    }

    @GetMapping(value = "/speed/getUpdates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<DatabaseUpdate<Speed>> getSpeedUpdates() {
        return CommonHelper.getChangeStream(reactiveMongo, Const.COLLECTION_SPEED, Speed.class);
    }

    @GetMapping(value = "/rotation/getUpdates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<DatabaseUpdate<Rotation>> getRotationUpdates() {
        return CommonHelper.getChangeStream(reactiveMongo, Const.COLLECTION_ROTATION, Rotation.class);
    }

    @GetMapping(value = "/getUpdates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<DatabaseUpdateNew<?>> getUpdates() {
        return Flux.merge(
                CommonHelper.getChangeStreamNew(reactiveMongo, Const.COLLECTION_GNSS, GNSS.class),
                CommonHelper.getChangeStreamNew(reactiveMongo, Const.COLLECTION_SPEED, Speed.class),
                CommonHelper.getChangeStreamNew(reactiveMongo, Const.COLLECTION_ROTATION, Rotation.class)
        );

//        return CommonHelper.getChangeStream(reactiveMongo, Const.COLLECTION_ROTATION, Rotation.class);
    }
}
