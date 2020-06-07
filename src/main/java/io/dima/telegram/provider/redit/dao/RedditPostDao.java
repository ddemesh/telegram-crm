package io.dima.telegram.provider.redit.dao;

import io.dima.telegram.provider.redit.model.RedditPost;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface RedditPostDao extends ReactiveMongoRepository<RedditPost, String> {
}
