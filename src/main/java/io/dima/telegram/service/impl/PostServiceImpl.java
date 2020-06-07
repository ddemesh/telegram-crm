package io.dima.telegram.service.impl;

import io.dima.telegram.dao.FuturePostDao;
import io.dima.telegram.dao.PostDao;
import io.dima.telegram.model.FuturePost;
import io.dima.telegram.model.Post;
import io.dima.telegram.service.PostService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PostServiceImpl implements PostService {

    private FuturePostDao futurePostDao;
    private PostDao postDao;

    public PostServiceImpl(FuturePostDao futurePostDao, PostDao postDao) {
        this.futurePostDao = futurePostDao;
        this.postDao = postDao;
    }

    @Override
    public FuturePost buildFuture(Post post) {
        FuturePost futurePost = new FuturePost();
        futurePost.setPost(post);
        return futurePost;
    }

    @Override
    public Flux<Post> findAll() {
        return postDao.findAll();
    }

    @Override
    public Flux<FuturePost> findAllFutures() {
        return futurePostDao.findAll();
    }

    @Override
    public Mono<Post> create(Post post) {
        post.setId(null);
        return postDao.save(post);
    }

    @Override
    public Mono<FuturePost> createFuture(FuturePost futurePost) {
        futurePost.setId(null);
        return futurePostDao.save(futurePost);
    }

    @Override
    public Mono<Post> update(Post post) {
        return postDao.findById(post.getId())
                .map(old -> {
                    old.setText(post.getText());
                    old.setMedia(post.getMedia());
                    return old;
                })
                .flatMap(postDao::save);
    }

    @Override
    public Mono<FuturePost> updateFuture(FuturePost futurePost) {
        return futurePostDao.findById(futurePost.getId())
                .map(old -> {
                    old.setPost(futurePost.getPost());
                    old.setDate(futurePost.getDate());
                    return old;
                })
                .flatMap(futurePostDao::save);
    }

    @Override
    public Mono<Post> delete(String id) {
        return postDao.findById(id)
                .flatMap(entity -> postDao.delete(entity).thenReturn(entity));
    }

    @Override
    public Mono<FuturePost> deleteFuture(String id) {
        return futurePostDao.findById(id)
                .flatMap(futurePost -> futurePostDao.delete(futurePost).thenReturn(futurePost));
    }
}
