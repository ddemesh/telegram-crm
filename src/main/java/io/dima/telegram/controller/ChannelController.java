package io.dima.telegram.controller;

import io.dima.telegram.model.Channel;
import io.dima.telegram.service.ChannelService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/channel")
public class ChannelController {
    private ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @GetMapping
    public Flux<Channel> get() {
        return channelService.get();
    }

    @PostMapping
    public Mono<Channel> create(@Valid @RequestBody Mono<Channel> channel) {
        return channelService.create(channel);
    }

    @PutMapping("/{id}")
    public Mono<Channel> save(@PathVariable String id, @Valid @RequestBody Mono<Channel> channel) {
        return channel.doOnNext(ch -> ch.setId(id))
                .flatMap(channelService::save);
    }

    @DeleteMapping("/{id}")
    public Mono<Channel> delete(@PathVariable String id) {
        return channelService.delete(id);
    }
}
