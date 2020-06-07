package io.dima.telegram.controller;

import io.dima.telegram.model.FuturePost;
import io.dima.telegram.model.Post;
import io.dima.telegram.service.PostService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/post")
public class PostController {

    private PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public Flux<Post> getAll() {
        return postService.findAll();
    }

    @GetMapping("futures")
    public Flux<FuturePost> getAllFutures() {
        return postService.findAllFutures();
    }

    @PutMapping("{id}")
    public Mono<Post> update(@PathVariable String id, @RequestBody Post post) {
        post.setId(id);
        return postService.update(post);
    }

    @PutMapping("futures/{id}")
    public Mono<FuturePost> updateFuture(@PathVariable String id, @RequestBody FuturePost futurePost) {
        futurePost.setId(id);
        return postService.updateFuture(futurePost);
    }
}
