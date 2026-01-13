package com.reactivo.capacidad.application.config;

import com.reactivo.capacidad.domain.api.CapacityServicePort;
import com.reactivo.capacidad.domain.api.TransactionalPort;
import com.reactivo.capacidad.domain.spi.CapacityPersistencePort;
import com.reactivo.capacidad.domain.spi.CapacityTechnologyClientPort;
import com.reactivo.capacidad.domain.usecase.CapacityUseCase;
import com.reactivo.capacidad.infrastructure.adapters.client.CapacityTechnologyClientAdapter;
import com.reactivo.capacidad.infrastructure.adapters.configuration.TransactionalPortSpring;
import com.reactivo.capacidad.infrastructure.adapters.persistence.capacity.CapacityPersistenceAdapter;
import com.reactivo.capacidad.infrastructure.adapters.persistence.capacity.mapper.CapacityEntityMapper;
import com.reactivo.capacidad.infrastructure.adapters.persistence.capacity.repository.CapacityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class UseCasesConfig {

    private final CapacityRepository capacityRepository;
    private final CapacityEntityMapper capacityEntityMapper;
    private final WebClient capacityTechnologyWebClient;
    private final TransactionalOperator transactionalOperator;
    private final DatabaseClient databaseClient;

    @Bean
    public CapacityPersistencePort capacityPersistencePort() {
        return new CapacityPersistenceAdapter(capacityRepository, capacityEntityMapper, databaseClient);
    }

    @Bean
    public CapacityTechnologyClientPort capacityTechnologyClientPort() {
        return new CapacityTechnologyClientAdapter(capacityTechnologyWebClient);
    }

    @Bean
    public TransactionalPort transactionalPort() {
        return new TransactionalPortSpring(transactionalOperator);
    }

    @Bean
    public CapacityServicePort capacityServicePort(CapacityPersistencePort capacityPersistencePort,
                                                   CapacityTechnologyClientPort capacityTechnologyClientPort,
                                                   TransactionalPort transactionalPort) {
        return new CapacityUseCase(capacityPersistencePort, capacityTechnologyClientPort, transactionalPort);
    }
}
