package io.dima.telegram.service.impl;

import io.dima.telegram.model.PostSchedule;
import io.dima.telegram.model.ScheduleInfo;
import io.dima.telegram.service.PostManagementService;
import io.dima.telegram.service.PostScheduleService;
import io.dima.telegram.service.SchedulingService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class PostScheduleServiceImpl implements PostScheduleService {
    private final SchedulingService schedulingService;
    private final PostManagementService managementService;

    public PostScheduleServiceImpl(SchedulingService schedulingService, PostManagementService managementService) {
        this.schedulingService = schedulingService;
        this.managementService = managementService;
    }

    @Override
    public Flux<PostSchedule> scheduleNext(PostSchedule schedule) {
        ScheduleInfo<PostSchedule> scheduleInfo = new ScheduleInfo<>(schedule, schedule.getDelays(), schedule.getTimes());
        return schedulingService.scheduleNext(scheduleInfo);
    }

    @Override
    public void stop(PostSchedule schedule) {
        ScheduleInfo<PostSchedule> scheduleInfo = new ScheduleInfo<>(schedule, schedule.getDelays(), schedule.getTimes());
        schedulingService.stop(scheduleInfo);
    }
}
