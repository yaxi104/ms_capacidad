package com.reactivo.capacidad.infrastructure.adapters.client.config;

import com.reactivo.capacidad.infrastructure.adapters.client.dto.CapacityTechnologyClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

import static com.reactivo.capacidad.infrastructure.entrypoints.util.Constants.AUTH_TOKEN;
import static com.reactivo.capacidad.infrastructure.entrypoints.util.Constants.X_MESSAGE_ID;

@Configuration
public class CapacityTechnologyClientConfig {

    @Bean
    public WebClient capacityTechnologyWebClient(
            CapacityTechnologyClientProperties properties) {

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(properties.getTimeoutMs()));

        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter((request, next) ->
                        Mono.deferContextual(ctx -> {

                            ClientRequest.Builder builder =
                                    ClientRequest.from(request);

                            if (ctx.hasKey(AUTH_TOKEN)) {
                                String token = ctx.get(AUTH_TOKEN);
                                if (token != null) {
                                    builder.header(HttpHeaders.AUTHORIZATION, token);
                                }
                            }

                            if (ctx.hasKey(X_MESSAGE_ID)) {
                                String messageId = ctx.get(X_MESSAGE_ID);
                                if (messageId != null) {
                                    builder.header(X_MESSAGE_ID, messageId);
                                }
                            }

                            return next.exchange(builder.build());
                        })
                )
                .build();
    }
}