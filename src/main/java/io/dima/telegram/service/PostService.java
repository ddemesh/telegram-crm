package io.dima.telegram.service;

import io.dima.telegram.model.FuturePost;
import io.dima.telegram.model.Post;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PostService {
    FuturePost buildFuture(Post post);

    Flux<Post> findAll();

    Flux<FuturePost> findAllFutures();

    Mono<Post> create(Post post);

    Mono<FuturePost> createFuture(FuturePost futurePost);

    Mono<Post> update(Post post);

    Mono<FuturePost> updateFuture(FuturePost futurePost);

    Mono<Post> delete(String id);

    Mono<FuturePost> deleteFuture(String id);
}
