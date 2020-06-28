package com.stakkato95.service.drone.helper;

import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

public final class CommonHelper {

    public static <T> Flux<DatabaseUpdate<T>> getChangeStream(ReactiveMongoTemplate template,
                                                              String collection,
                                                              Class<T> clazz) {
        return template.changeStream(Const.DB_NAME, collection, ChangeStreamOptions.empty(), clazz)
                .map(e -> {
                    DatabaseUpdate<T> update = new DatabaseUpdate<>();
                    update.id = BsonHelper.getObjectId(e);
                    update.object = e.getBody();
                    return update;
                });
    }

    public static <T> Flux<DatabaseUpdateNew<T>> getChangeStreamNew(ReactiveMongoTemplate template,
                                                                    String collection,
                                                                    Class<T> clazz) {
        return template.changeStream(Const.DB_NAME, collection, ChangeStreamOptions.empty(), clazz)
                .map(e -> {
                    DatabaseUpdateNew<T> update = new DatabaseUpdateNew<>();
                    update.id = BsonHelper.getObjectId(e);
                    update.object = e.getBody();
                    update.collection = e.getCollectionName();
                    return update;
                });
    }
}
