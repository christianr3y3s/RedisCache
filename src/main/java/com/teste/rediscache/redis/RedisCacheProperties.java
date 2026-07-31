package com.teste.rediscache.redis;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(
        prefix = "nfe.cache"
)
public class RedisCacheProperties {

    private boolean enabled = true;

    private String keyPrefix = "nfe";

    private Duration ttl =
            Duration.ofHours(24);
}