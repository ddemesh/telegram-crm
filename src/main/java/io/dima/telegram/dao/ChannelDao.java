package io.dima.telegram.dao;

import io.dima.telegram.model.Channel;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ChannelDao extends ReactiveMongoRepository<Channel, String> {
}
