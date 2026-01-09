package com.reactivo.capacidad.infrastructure.adapters.client.dto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "clients.capacity-technology")
public class CapacityTechnologyClientProperties {

    private String baseUrl;
    private long timeoutMs;
}
