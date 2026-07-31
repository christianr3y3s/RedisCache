package com.teste.rediscache.redis.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teste.rediscache.redis.exceptions.RedisCacheException;
import com.teste.rediscache.redis.model.NfeCacheEntry;
import com.teste.rediscache.redis.repository.RedisNfeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NfeCacheService {

    private final ObjectMapper objectMapper;
    private final RedisNfeRepository repository;

    public void processar(String payload) {

        try {

            NfeCacheEntry nfe =
                    objectMapper.readValue(
                            payload,
                            NfeCacheEntry.class
                    );

            repository.salvar(nfe);

        } catch (Exception ex) {

            throw new RedisCacheException(
                    "Erro ao processar NF-e",
                    ex
            );
        }
    }
}