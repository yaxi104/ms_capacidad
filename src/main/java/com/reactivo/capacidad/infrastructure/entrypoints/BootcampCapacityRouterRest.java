package com.reactivo.capacidad.infrastructure.entrypoints;

import com.reactivo.capacidad.infrastructure.entrypoints.handler.BootcampCapacityHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class BootcampCapacityRouterRest {

    @Bean("bootcampCapacityRouter")
    public RouterFunction<ServerResponse> routerFunction(BootcampCapacityHandler bootcampCapacityHandler) {
        return RouterFunctions
                .route(POST("/bootcamp-capacidad").and(accept(MediaType.APPLICATION_JSON)), bootcampCapacityHandler::saveAllBoorcampCapacity);
//                .andRoute(GET("/capacidad-tecnologia/{idCapacidad}").and(accept(MediaType.APPLICATION_JSON)), capacityTechnologyHandler::findAllIdTechnologyByIdCapacity)
//                .andRoute(POST("/tecnologias/capacidades").and(accept(MediaType.APPLICATION_JSON)), capacityTechnologyHandler::getTechnologiesByCapacityIds)
//                .andRoute(GET("/capacidad-tecnologias").and(accept(MediaType.APPLICATION_JSON)), capacityTechnologyHandler::getCapacityIdGroupedTechnologies);
    }
}
