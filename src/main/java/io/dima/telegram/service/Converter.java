package io.dima.telegram.service;

import reactor.core.publisher.Flux;

public interface Converter<T, U> {
    Flux<U> convert(T source);
}
