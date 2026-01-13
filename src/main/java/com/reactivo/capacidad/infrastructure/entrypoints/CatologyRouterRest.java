package com.reactivo.capacidad.infrastructure.entrypoints;

import com.reactivo.capacidad.infrastructure.entrypoints.handler.CapacityHandlerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class CatologyRouterRest {

    @Bean("capacityyRouter")
    public RouterFunction<ServerResponse> routerFunction(CapacityHandlerImpl capacityHandler) {
        return RouterFunctions
                .route(POST("/capacidades").and(accept(MediaType.APPLICATION_JSON)), capacityHandler::createCapacity);
    }
}