package io.dima.telegram.provider.redit.service.impl;

import io.dima.telegram.provider.redit.model.RedditPost;
import io.dima.telegram.service.Converter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Log4j2
public class RedditPostConverter implements Converter<Map<String, Object>, RedditPost> {
    @Override
    public Flux<RedditPost> convert(Map<String, Object> source) {
        List<LinkedHashMap> children = (List<LinkedHashMap>) ((LinkedHashMap<String, Object>) source.get("data")).get("children");
        log.info("Children count: " + children.size());
        return Flux.fromStream(children
                .parallelStream()
//                todo check how it can be used
//                .filter(linkedHashMap -> "t3".equals(linkedHashMap.get("kind")))
                .map(linkedHashMap -> convertToPost((LinkedHashMap) linkedHashMap.get("data")))
                .filter(Objects::nonNull));
    }

    private RedditPost convertToPost(LinkedHashMap map) {
        String id = (String) map.get("name");
        String url;
        String title = (String) map.get("title");
        boolean video = false;
        if ((Boolean) map.get("is_video")) {
            LinkedHashMap redditVideo = ((LinkedHashMap<String, LinkedHashMap>) map.get("media")).get("reddit_video");
            url = (String) redditVideo.get("fallback_url");
            video = true;
        } else {
            url = (String) map.get("url");
        }
        RedditPost post = new RedditPost(id, title, url, video);
        log.info("Created post: " + post);
        return post;
    }
}
