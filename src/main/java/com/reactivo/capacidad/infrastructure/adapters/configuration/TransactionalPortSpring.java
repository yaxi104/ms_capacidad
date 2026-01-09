package com.reactivo.capacidad.infrastructure.adapters.configuration;

import com.reactivo.capacidad.domain.api.TransactionalPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class TransactionalPortSpring implements TransactionalPort {

    private final TransactionalOperator transactionalOperator;

    @Override
    public <T> Mono<T> transactional(Mono<T> action) {
        return transactionalOperator.transactional(action);
    }
}