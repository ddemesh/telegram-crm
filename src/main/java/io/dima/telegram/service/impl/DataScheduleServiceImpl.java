package io.dima.telegram.service.impl;

import io.dima.telegram.model.DataSchedule;
import io.dima.telegram.model.Post;
import io.dima.telegram.model.ScheduleInfo;
import io.dima.telegram.service.DataScheduleService;
import io.dima.telegram.service.PostProvider;
import io.dima.telegram.service.PostService;
import io.dima.telegram.service.SchedulingService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class DataScheduleServiceImpl implements DataScheduleService {

    private final PostProvider redditProviderService;
    private final SchedulingService schedulingService;
    private PostService postService;

    public DataScheduleServiceImpl(PostProvider redditProviderService, SchedulingService schedulingService, PostService postService) {
        this.redditProviderService = redditProviderService;
        this.schedulingService = schedulingService;
        this.postService = postService;
    }

    @Override
    public Flux<Post> scheduleNext(DataSchedule schedule) {
        ScheduleInfo<DataSchedule> scheduleInfo = new ScheduleInfo<>(schedule, schedule.getDelays(), schedule.getTimes());
        Flux<Post> flux = redditProviderService.loadPost(schedulingService.scheduleNext(scheduleInfo))
                .doOnNext(post -> post.setWatched(schedule.isAutomated()))
                .flatMap(postService::create);
        return flux;
    }

    @Override
    public void stop(DataSchedule schedule) {
        ScheduleInfo<DataSchedule> scheduleInfo = new ScheduleInfo<>(schedule, schedule.getDelays(), schedule.getTimes());
        schedulingService.stop(scheduleInfo);
    }

}
