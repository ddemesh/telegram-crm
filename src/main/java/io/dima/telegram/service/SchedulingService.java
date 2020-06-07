package io.dima.telegram.service;

import io.dima.telegram.model.ScheduleInfo;
import reactor.core.publisher.Flux;

public interface SchedulingService {
    <T> Flux<T> scheduleNext(ScheduleInfo<T> scheduleInfo);

    <T> void stop(ScheduleInfo<T> scheduleInfo);
}
