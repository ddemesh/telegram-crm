package io.dima.telegram.service;

import io.dima.telegram.model.Channel;
import io.dima.telegram.model.FuturePost;
import io.dima.telegram.model.Post;
import reactor.core.publisher.Mono;

public interface PostManagementService {
    Mono<Post> post(Channel channel, FuturePost futurePost);
}
