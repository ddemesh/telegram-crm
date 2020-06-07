package io.dima.telegram.provider.redit.service;

import reactor.core.publisher.Mono;

public interface RedditAuthenticationService {
    Mono<String> loadToken(String clientId, String clientSecret);
}
