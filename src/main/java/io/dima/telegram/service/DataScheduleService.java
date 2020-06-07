package io.dima.telegram.service;

import io.dima.telegram.model.DataSchedule;
import io.dima.telegram.model.Post;
import reactor.core.publisher.Flux;

public interface DataScheduleService {

    Flux<Post> scheduleNext(DataSchedule schedule);

    void stop(DataSchedule schedule);
}
