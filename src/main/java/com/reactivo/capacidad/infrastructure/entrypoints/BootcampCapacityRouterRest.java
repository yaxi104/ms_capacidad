package com.reactivo.capacidad.infrastructure.entrypoints;

import com.reactivo.capacidad.infrastructure.entrypoints.handler.BootcampCapacityHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class BootcampCapacityRouterRest {

    @Bean("bootcampCapacityRouter")
    public RouterFunction<ServerResponse> routerFunction(BootcampCapacityHandler bootcampCapacityHandler) {
        return RouterFunctions
                .route(POST("/bootcamp-capacidad").and(accept(MediaType.APPLICATION_JSON)), bootcampCapacityHandler::saveAllBoorcampCapacity)
                .andRoute(POST("/capacidades/bootcamps").and(accept(MediaType.APPLICATION_JSON)), bootcampCapacityHandler::getCapacitiesByBootcampIds)
                .andRoute(GET("/bootcamp-capacidades").and(accept(MediaType.APPLICATION_JSON)), bootcampCapacityHandler::getBootcampIdGroupedCapacities);
    }
}
