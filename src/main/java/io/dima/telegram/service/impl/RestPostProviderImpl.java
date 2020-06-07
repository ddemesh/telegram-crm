package io.dima.telegram.service.impl;

import io.dima.telegram.model.DataSchedule;
import io.dima.telegram.model.Post;
import io.dima.telegram.service.PostProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

//todo advanced improvement
//@Service
public class RestPostProviderImpl implements PostProvider {
    private WebClient client;

    public RestPostProviderImpl() {
        client = WebClient.create();
    }

    @Override
    public Flux<Post> loadPost(Flux<DataSchedule> trigger) {
        return trigger.map(o -> new Post());
    }

//    private
}
