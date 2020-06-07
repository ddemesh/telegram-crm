package io.dima.telegram.provider.redit.service.impl;

import io.dima.telegram.model.DataSchedule;
import io.dima.telegram.model.Post;
import io.dima.telegram.model.ScheduleInfo;
import io.dima.telegram.provider.redit.dao.RedditPostDao;
import io.dima.telegram.provider.redit.model.RedditPost;
import io.dima.telegram.provider.redit.service.RedditAuthenticationService;
import io.dima.telegram.service.Converter;
import io.dima.telegram.service.PostProvider;
import io.dima.telegram.service.PostService;
import io.dima.telegram.service.SchedulingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service("redditPostProvider")
public class RedditProviderServiceImpl implements PostProvider {

    private static long REDDIT_TOKEN_EXPIRES_IN = TimeUnit.SECONDS.toMillis(3600);
    private static String REDDIT_SCHEDULE_ID = "REDDIT_SCHEDULE_ID";
    private static int REDDIT_POST_LIMIT = 25;

    private boolean init = false;
    private AtomicInteger subscribeCount = new AtomicInteger();

    @Value("${io.dima.reddit.auth.client_id}")
    private String clientId;
    @Value("${io.dima.reddit.auth.client_secret}")
    private String clientSecret;

    private String token;

    private SchedulingService schedulingService;
    private RedditAuthenticationService authenticationService;
    private Converter<Map<String, Object>, RedditPost> postConverter;
    private RedditPostDao redditPostDao;

    private WebClient webClient = WebClient.create();

    public RedditProviderServiceImpl(SchedulingService schedulingService, RedditAuthenticationService authenticationService, Converter<Map<String, Object>, RedditPost> postConverter, RedditPostDao redditPostDao) {
        this.schedulingService = schedulingService;
        this.authenticationService = authenticationService;
        this.postConverter = postConverter;
        this.redditPostDao = redditPostDao;
    }

    @Override
    public Flux<Post> loadPost(Flux<DataSchedule> trigger) {
        return init().thenMany(trigger.flatMap(this::loadPost));
    }

    private Flux<Post> loadPost(DataSchedule schedule) {
        return loadPost(schedule, 0, REDDIT_POST_LIMIT)
                .map(redditPost -> {
                    Post post = new Post();
                    post.setText(redditPost.getTitle());
                    post.setMedia(redditPost.getUrl());
                    post.setVideo(redditPost.isVideo());
                    return post;
                });
    }

    private Flux<RedditPost> loadPost(DataSchedule schedule, int count, int limit) {
        return webClient.method(HttpMethod.GET)
                .uri("https://oauth.reddit.com/" + schedule.getUrl() + "/new?limit=" + limit + "&count=" + count)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .flatMapMany(postConverter::convert)
                .filterWhen(redditPost -> redditPostDao.existsById(redditPost.getId()).map(val -> !val))
//                todo implement situation when all loaded posts've been processed
//                .transform(redditPostFlux -> redditPostFlux.switchIfEmpty(loadPost(schedule, count + limit, limit)))
                .flatMap(redditPostDao::save);
    }

    public Mono<Void> init() {
        if (!init) {
            ScheduleInfo<String> scheduleInfo = new ScheduleInfo<>(REDDIT_SCHEDULE_ID, Collections.singletonList(REDDIT_TOKEN_EXPIRES_IN), Collections.emptyList());
            Flux<String> flux = schedulingService.scheduleNext(scheduleInfo)
                    .flatMap(s -> authenticationService.loadToken(clientId, clientSecret))
                    .doOnNext(s -> token = s)
                    .switchOnFirst((signal, stringFlux) -> {
                        if (signal.hasValue()) {
                            init = true;
                        }
                        return stringFlux;
                    })
                    .share();
            flux.subscribe();
            return flux
                    .next()
                    .then();
        } else {
            return Mono.empty();
        }
    }

    public Mono<Void> stop() {
        if (!init) {
            return  null;
        } else {
            return Mono.empty();
        }
    }
}
