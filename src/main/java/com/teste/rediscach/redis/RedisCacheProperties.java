package com.teste.rediscach.redis;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "teste.cache.redis")
public class RedisCacheProperties {

    private boolean enabled = true;

    @NotBlank
    private String keyPrefix = "cache";

    private String namespace;

    private Duration ttl = Duration.ofMinutes(30);

    private boolean verboseLogging = false;
}