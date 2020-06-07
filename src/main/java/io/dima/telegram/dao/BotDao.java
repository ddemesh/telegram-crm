package io.dima.telegram.dao;

import io.dima.telegram.model.Bot;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface BotDao extends ReactiveMongoRepository<Bot, String> {
}
