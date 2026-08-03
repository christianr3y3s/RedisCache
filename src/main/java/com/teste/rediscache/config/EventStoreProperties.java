package com.teste.rediscache.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "event.store")
public class EventStoreProperties {

    private boolean enabled = true;

    @NotBlank
    private String topic = "event-store";

    private boolean verboseLogging = false;
}