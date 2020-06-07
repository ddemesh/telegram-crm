package io.dima.telegram.service;

import io.dima.telegram.model.PostSchedule;
import reactor.core.publisher.Flux;

public interface PostScheduleService {

    Flux<PostSchedule> scheduleNext(PostSchedule schedule);

    void stop(PostSchedule schedule);
}
