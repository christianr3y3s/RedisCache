package com.teste.rediscache.redis.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teste.rediscache.redis.RedisCacheProperties;
import com.teste.rediscache.redis.exceptions.RedisCacheException;
import com.teste.rediscache.redis.model.NfeCacheEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisNfeRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisCacheProperties properties;

    public void salvar(NfeCacheEntry nfe) {

        try {

            String key =
                    buildKey(
                            nfe.getChaveAcesso()
                    );

            String payload =
                    objectMapper.writeValueAsString(nfe);

            redisTemplate
                    .opsForValue()
                    .set(
                            key,
                            payload,
                            properties.getTtl()
                    );

        } catch (Exception ex) {

            throw new RedisCacheException(
                    "Erro ao salvar NF-e",
                    ex
            );
        }
    }

    private String buildKey(String chave) {

        return "%s:%s"
                .formatted(
                        properties.getKeyPrefix(),
                        chave
                );
    }
}