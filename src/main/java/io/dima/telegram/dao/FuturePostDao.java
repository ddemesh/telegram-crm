package io.dima.telegram.dao;

import io.dima.telegram.model.FuturePost;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface FuturePostDao extends ReactiveMongoRepository<FuturePost, String> {
}
