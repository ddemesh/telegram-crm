package io.dima.telegram.provider.redit.service.impl;

import io.dima.telegram.provider.redit.service.RedditAuthenticationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class RedditAuthenticationServiceImpl implements RedditAuthenticationService {

    private static final String TOKEN_URL = "https://www.reddit.com/api/v1/access_token";
    public static final String GRANT_TYPE_PARAM = "grant_type";
    public static final String GRANT_TYPE = "client_credentials";
    public static final String ACCESS_TOKEN_KEY = "access_token";

    private WebClient webClient;

    public RedditAuthenticationServiceImpl() {
        webClient = WebClient.create(TOKEN_URL);
    }

    @Override
    public Mono<String> loadToken(String clientId, String clientSecret) {
        String authorization = "Basic " + Base64Utils.encodeToString((clientId + ":" + clientSecret).getBytes());
        return webClient.post()
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(GRANT_TYPE_PARAM, GRANT_TYPE))
                .retrieve()
                .bodyToMono(Map.class)
                .map(map -> (String) map.get(ACCESS_TOKEN_KEY));
    }
}
