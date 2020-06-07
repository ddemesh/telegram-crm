package io.dima.telegram.service.impl;

import io.dima.telegram.model.Channel;
import io.dima.telegram.model.FuturePost;
import io.dima.telegram.model.Post;
import io.dima.telegram.service.PostManagementService;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class PostManagementServiceImpl implements PostManagementService {

    private static final String SEND_TEXT_URL = "https://api.telegram.org/bot<token>/sendMessage";
    private static final String SEND_PHOTO_URL = "https://api.telegram.org/bot<token>/sendPhoto";
    private static final String SEND_VIDEO_URL = "https://api.telegram.org/bot<token>/sendVideo";
    private static final String SEND_ANIMATION_URL = "https://api.telegram.org/bot<token>/sendAnimation";

    private List<String> imageExtensions = Arrays.asList("png", "jpg", "jpeg");
    private List<String> animationExtensions = Arrays.asList("gif");

    private WebClient webClient;

    public PostManagementServiceImpl(WebClient.Builder webClientBuilder) {
        HttpClient httpClient = HttpClient.create()
                .tcpConfiguration(tcpClient -> {
                    tcpClient = tcpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000);
                    tcpClient = tcpClient.doOnConnected(conn -> conn
                            .addHandlerLast(new ReadTimeoutHandler(60000, TimeUnit.MILLISECONDS)));
                    return tcpClient;
                });
        // create a client http connector using above http client
        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        // use this configured http connector to build the web client
        this.webClient = webClientBuilder.clientConnector(connector).build();
    }

    @Override
    public Mono<Post> post(Channel channel, FuturePost futurePost) {
        Post post = futurePost.getPost();
        String media = post.getMedia();
        Mono<ClientResponse> exchange;
        if (post.isVideo()) {
            exchange = sendVideo(channel, post);
        } else if (media == null) {
            exchange = sendText(channel, post.getText());
        } else {
            String extension = UriUtils.extractFileExtension(media);
            if (imageExtensions.contains(extension)) {
                exchange = sendPhoto(channel, post);
            } else if (animationExtensions.contains(extension)) {
                exchange = sendAnimation(channel, post);
            } else {
                exchange = sendText(channel, createTextWithLink(post));
            }
        }

        log.info("Preparing to post: " + post);
        return exchange.flatMap(clientResponse -> clientResponse.bodyToMono(Map.class))
                .doOnError(throwable -> log.error("Error posting: " + throwable))
                .flatMap(map -> {
                    log.info("Response: " + map);
                    if ((Boolean) map.get("ok")) {
                        return Mono.just(post);
                    }
                    log.error("Error to send telegram message: " + map);
                    return Mono.empty();
                });
    }

    private String createTextWithLink(Post post) {
        return post.getText() + "\n" + post.getMedia();
    }

    private String createTextWithLink(String message, Post post) {
        return message + "\n" + createTextWithLink(post);
    }

    private Mono<ClientResponse> sendPhoto(Channel channel, Post post) {
        return loadFile(post.getMedia())
                .flatMap(bytes -> createMediaRequest(channel, post, bytes, "photo", SEND_PHOTO_URL))
                .onErrorResume(throwable -> sendText(channel, createTextWithLink("Error with sending photo:", post)));
    }

    private Mono<ClientResponse> sendVideo(Channel channel, Post post) {
        return loadFile(post.getMedia())
                .flatMap(bytes -> createMediaRequest(channel, post, bytes, "video", SEND_VIDEO_URL))
                .onErrorResume(throwable -> sendText(channel, createTextWithLink("Error with sending video:", post)));
    }

    private Mono<ClientResponse> sendAnimation(Channel channel, Post post) {
        return loadFile(post.getMedia())
                .flatMap(bytes -> createMediaRequest(channel, post, bytes, "animation", SEND_ANIMATION_URL))
                .onErrorResume(throwable -> sendText(channel, createTextWithLink("Error with sending animation:", post)));
    }

    private Mono<ClientResponse> sendText(Channel channel, String text) {
        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("chat_id", channel.getChatIdentifier());
        multipartBodyBuilder.part("text", text);
        return createTelegramRequest(channel.getBot().getToken(), multipartBodyBuilder.build(), SEND_TEXT_URL);
    }

    private Mono<ClientResponse> createMediaRequest(Channel channel, Post post, ByteArrayResource bytes, String mediaKey, String url) {
        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("chat_id", channel.getChatIdentifier());
        multipartBodyBuilder.part(mediaKey, bytes).filename(FilenameUtils.getName(post.getMedia()));
        multipartBodyBuilder.part("caption", post.getText());
        return createTelegramRequest(channel.getBot().getToken(), multipartBodyBuilder.build(), url);
    }

    private Mono<ClientResponse> createTelegramRequest(String token, MultiValueMap<String, HttpEntity<?>> params, String url) {
        String uri = url.replace("<token>", token);
        log.info("Sending requests to " + uri);
        return webClient.post()
                .uri(uri)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(params))
                .exchange();
    }

    private Mono<ByteArrayResource> loadFile(String url) {
        return webClient.get()
                .uri(url)
                .exchange()
                .flatMap(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(ByteArrayResource.class);
                    }
                    return Mono.error(new RuntimeException("Cant load file " + url + ": " + response.body(BodyExtractors.toDataBuffers())));
                })
                .doOnError(throwable -> log.error("Exception while loading file: " + throwable));
    }
}
