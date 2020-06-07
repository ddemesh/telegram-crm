package io.dima.telegram.service.impl;

import io.dima.telegram.dao.ChannelDao;
import io.dima.telegram.model.Channel;
import io.dima.telegram.model.FuturePost;
import io.dima.telegram.service.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Service
@Log4j2
public class ChannelServiceImpl implements ChannelService {

    @Value("${RESTORE_CHANNELS:true}")
    private boolean restore;

    private ChannelDao channelDao;
    private DataScheduleService dataScheduleService;
    private PostScheduleService postScheduleService;
    private PostService postService;
    private PostManagementService managementService;

    public ChannelServiceImpl(ChannelDao channelDao, DataScheduleService dataScheduleService, PostScheduleService postScheduleService, PostService postService, PostManagementService managementService) {
        this.channelDao = channelDao;
        this.dataScheduleService = dataScheduleService;
        this.postScheduleService = postScheduleService;
        this.postService = postService;
        this.managementService = managementService;
    }

    @PostConstruct
    private void restore() {
        log.info("Restoring channel scheduling: " + restore);
        if (!restore) return;
        channelDao.findAll()
                .doOnNext(this::runDataSchedules)
                .doOnNext(this::runPostSchedules)
                .subscribe();
    }

    @Override
    public Flux<Channel> get() {
        return channelDao.findAll();
    }

    @Override
    public Mono<Channel> create(Mono<Channel> channel) {
        return channel
                .map(ch -> {
                    ch.setId(null);
                    return ch;
                })
                .flatMap(channelDao::save)
                .doOnNext(this::runDataSchedules)
                .doOnNext(this::runPostSchedules);
    }

    @Override
    public Mono<Channel> save(Channel channel) {
        return channelDao.findById(channel.getId())
                .doOnNext(this::stopDataSchedules)
                .doOnNext(this::stopPostSchedules)
                .map(old -> {
                    old.setChatIdentifier(channel.getChatIdentifier());
                    old.setDataSchedules(channel.getDataSchedules());
                    old.setDataSchedules(channel.getDataSchedules());
                    old.setBot(channel.getBot());
                    return old;
                })
                .flatMap(channelDao::save)
                .doOnNext(this::runDataSchedules)
                .doOnNext(this::runPostSchedules);
    }

    @Override
    public Mono<Channel> delete(String id) {
        return channelDao.findById(id)
                .doOnNext(this::stopDataSchedules)
                .doOnNext(this::stopPostSchedules)
                .doOnNext(entity -> channelDao.delete(entity).subscribe());
    }

    private void runDataSchedules(Channel channel) {
        Flux.fromIterable(channel.getDataSchedules())
                .parallel()
                .flatMap(dataScheduleService::scheduleNext)
                .map(postService::buildFuture)
                //todo test logic
                .flatMap(postService::createFuture)
                .flatMap(futurePost -> {
                    channel.getFuturePosts().add(futurePost);
                    return channelDao.save(channel);
                })
                .subscribe();
    }

    private void stopDataSchedules(Channel channel) {
        Flux.fromIterable(channel.getDataSchedules())
                .parallel()
                .doOnNext(dataScheduleService::stop)
                .subscribe();
    }

    private void runPostSchedules(Channel channel) {
        Flux.fromIterable(channel.getPostSchedules())
                .parallel()
                .flatMap(postScheduleService::scheduleNext)
                .flatMap(postSchedule -> channelDao.findById(channel.getId()))
                .flatMap(ch -> ch.getFuturePosts()
                        .stream()
                        .filter(futurePost -> futurePost.getPost().isWatched())
                        .map(Mono::just)
                        .findFirst()
                        .orElse(Mono.empty()))
                .flatMap(futurePost -> managementService.post(channel, futurePost).thenReturn(futurePost))
                .flatMap(futurePost -> postService.deleteFuture(futurePost.getId()))
                .doOnNext(futurePost -> channel.getFuturePosts().remove(futurePost))
                .map(FuturePost::getPost)
                .flatMap(postService::update)
                .doOnNext(post -> channel.getPosts().add(post))
                .flatMap(post -> channelDao.save(channel))
                .subscribe();
    }

    private void stopPostSchedules(Channel channel) {
        Flux.fromIterable(channel.getPostSchedules())
                .parallel()
                .doOnNext(postScheduleService::stop)
                .subscribe();
    }
}
