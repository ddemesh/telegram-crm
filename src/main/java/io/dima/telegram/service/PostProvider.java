package io.dima.telegram.service;

import io.dima.telegram.model.DataSchedule;
import io.dima.telegram.model.Post;
import reactor.core.publisher.Flux;

public interface PostProvider {
    Flux<Post> loadPost(Flux<DataSchedule> trigger);
}
