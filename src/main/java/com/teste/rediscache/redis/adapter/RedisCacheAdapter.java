package com.teste.rediscach.redis.adapter;

import com.teste.rediscach.redis.RedisCacheClient;
import com.teste.rediscach.redis.RedisCacheProperties;
import com.teste.rediscach.redis.exceptions.RedisCacheException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheAdapter implements RedisCacheClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheAdapter.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisCacheProperties properties;

    @Override
    public <T> void put(String key, T value, Duration ttl) {
        String cacheKey = buildKey(key);
        Duration cacheTtl = ttl != null ? ttl : properties.getTtl();

        try {
            String payload = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(cacheKey, payload, cacheTtl);

            LOGGER.info(
                    "Cache Redis gravado com sucesso para chave='{}', ttl='{}'",
                    cacheKey,
                    cacheTtl
            );
        } catch (JsonProcessingException exception) {
            LOGGER.error(
                    "Erro ao serializar valor para cache Redis na chave='{}'",
                    cacheKey,
                    exception
            );
            throw new RedisCacheException("Erro ao serializar valor para cache Redis", exception);
        }
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        String cacheKey = buildKey(key);

        try {
            String payload = redisTemplate.opsForValue().get(cacheKey);

            if (payload == null) {
                LOGGER.info("Cache Redis não encontrado para chave='{}'", cacheKey);
                return Optional.empty();
            }

            T value = objectMapper.readValue(payload, type);

            LOGGER.info("Cache Redis encontrado para chave='{}'", cacheKey);
            return Optional.of(value);
        } catch (JsonProcessingException exception) {
            LOGGER.error(
                    "Erro ao desserializar valor do cache Redis para chave='{}'",
                    cacheKey,
                    exception
            );
            throw new RedisCacheException("Erro ao desserializar valor do cache Redis", exception);
        }
    }

    @Override
    public void evict(String key) {
        String cacheKey = buildKey(key);
        redisTemplate.delete(cacheKey);

        LOGGER.info("Cache Redis removido para chave='{}'", cacheKey);
    }

    @Override
    public boolean exists(String key) {
        String cacheKey = buildKey(key);
        Boolean exists = redisTemplate.hasKey(cacheKey);

        return Boolean.TRUE.equals(exists);
    }

    private String buildKey(String key) {
        return properties.getKeyPrefix() + ":" + key;
    }
}

