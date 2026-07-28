package com.teste.rediscach.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Setter
@Getter
@ConfigurationProperties(prefix = "teste.cache.redis")
public class RedisCacheProperties {

    private boolean enabled = true;
    private String keyPrefix = "app";
    private Duration defaultTtl = Duration.ofMinutes(30);

}
