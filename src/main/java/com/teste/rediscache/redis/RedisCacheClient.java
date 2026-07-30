package com.teste.rediscache.redis;
import java.time.Duration;
import java.util.Optional;

public interface RedisCacheClient {

        <T> void put(String key, T value, Duration ttl);

        <T> Optional<T> get(String key, Class<T> type);

        void evict(String key);

        boolean exists(String key);
}
