package io.dima.telegram.service;

import io.dima.telegram.model.Channel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChannelService {
    Flux<Channel> get();
    Mono<Channel> create(Mono<Channel> channel);
    Mono<Channel> save(Channel channel);
    Mono<Channel> delete(String id);
}
