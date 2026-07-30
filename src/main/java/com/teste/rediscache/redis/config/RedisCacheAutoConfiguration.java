package com.teste.rediscache.redis.config;

import com.teste.rediscach.redis.RedisCacheProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties(RedisCacheProperties.class)
public class RedisCacheAutoConfiguration {
}