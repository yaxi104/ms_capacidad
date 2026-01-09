package com.reactivo.capacidad.domain.api;

import reactor.core.publisher.Mono;

public interface TransactionalPort {
    <T> Mono<T> transactional(Mono<T> operation);
}