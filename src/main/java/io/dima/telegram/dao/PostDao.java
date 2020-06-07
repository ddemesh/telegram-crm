package io.dima.telegram.dao;

import io.dima.telegram.model.Post;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface PostDao extends ReactiveMongoRepository<Post, String> {
}
