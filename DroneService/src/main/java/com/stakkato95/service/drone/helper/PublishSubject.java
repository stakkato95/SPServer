package com.stakkato95.service.drone.helper;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.FluxSink;

public class PublishSubject<T> {

    private final EmitterProcessor<T> emitter = EmitterProcessor.create();
    private final FluxSink<T> sink = emitter.sink();

    public void next(T t) {
        sink.next(t);
    }

    public EmitterProcessor<T> getEmitter() {
        return emitter;
    }
}
