package com.reactivo.capacidad.infrastructure.config;

import com.reactivo.capacidad.infrastructure.adapters.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static com.reactivo.capacidad.infrastructure.entrypoints.util.Constants.USER_ADMIN;

@EnableWebFluxSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/public/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/capacidades").hasRole(USER_ADMIN)
                        .pathMatchers(HttpMethod.GET, "/capacidades/paginado").hasRole(USER_ADMIN)
                        .pathMatchers(HttpMethod.POST, "/bootcamp-capacidad").hasRole(USER_ADMIN)
                        .pathMatchers(HttpMethod.POST, "/capacidades/bootcamps").hasRole(USER_ADMIN)
                        .pathMatchers(HttpMethod.GET, "/bootcamp-capacidades").hasRole(USER_ADMIN)
                        .pathMatchers(HttpMethod.DELETE, "/capacidades/eliminar-por-bootcamp/**").hasRole(USER_ADMIN)
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}