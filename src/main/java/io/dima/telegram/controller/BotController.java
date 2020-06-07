package io.dima.telegram.controller;

import io.dima.telegram.dao.BotDao;
import io.dima.telegram.model.Bot;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/bot")
public class BotController {
    private BotDao botDao;

    public BotController(BotDao botDao) {
        this.botDao = botDao;
    }

    @GetMapping
    public Flux<Bot> get() {
        return botDao.findAll();
    }

    @PostMapping
    public Mono<Bot> create(@Valid @RequestBody Mono<Bot> botMono) {
        return botMono.doOnNext(bot -> bot.setId(null))
                .flatMap(botDao::save);
    }

    @PutMapping("/{id}")
    public Mono<Bot> save(@PathVariable String id, @Valid @RequestBody Bot bot) {
        return botDao.findById(id)
                .doOnNext(oldBot -> {
                    oldBot.setName(bot.getName());
                    oldBot.setToken(bot.getToken());
                })
                .flatMap(botDao::save);
    }

    @DeleteMapping("/{id}")
    public Mono<Bot> delete(@PathVariable String id) {
        return botDao.findById(id)
                .flatMap(bot -> botDao.deleteById(id).thenReturn(bot));
    }
}
