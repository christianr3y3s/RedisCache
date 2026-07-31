package com.teste.rediscache.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teste.rediscache.redis.RedisCacheProperties;
import com.teste.rediscache.redis.model.Cliente;
import com.teste.rediscache.redis.adapter.RedisCacheAdapter;
import com.teste.rediscache.redis.exceptions.RedisCacheException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisCacheAdapterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisCacheAdapter adapter;

    @BeforeEach
    void setup() {
        RedisCacheProperties properties = new RedisCacheProperties();
        properties.setKeyPrefix("redis");
        properties.setTtl(Duration.ofMinutes(30));

        adapter =
                new RedisCacheAdapter(
                        redisTemplate,
                        objectMapper,
                        properties
                );
    }

    @Test
    void shouldPutWithProvidedTtl() throws Exception {

        Cliente cliente = new Cliente("teste", 42);

        when(objectMapper.writeValueAsString(cliente))
                .thenReturn("{\"nome\":\"teste\"}");

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        adapter.put(
                "123",
                cliente,
                Duration.ofMinutes(10)
        );

        verify(valueOperations)
                .set(
                        eq("redis:123"),
                        eq("{\"nome\":\"teste\"}"),
                        eq(Duration.ofMinutes(10))
                );
    }

    @Test
    void shouldPutUsingDefaultTtlWhenNull() throws Exception {

        Cliente cliente = new Cliente("teste", 42);

        when(objectMapper.writeValueAsString(cliente))
                .thenReturn("{\"nome\":\"teste\"}");

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        adapter.put(
                "123",
                cliente,
                null
        );

        verify(valueOperations)
                .set(
                        eq("redis:123"),
                        eq("{\"nome\":\"teste\"}"),
                        eq(Duration.ofMinutes(30))
                );
    }

    @Test
    void shouldThrowRedisCacheExceptionWhenSerializationFails() throws Exception {

        Cliente cliente = new Cliente("teste", 42);

        when(objectMapper.writeValueAsString(cliente))
                .thenThrow(mock(JsonProcessingException.class));

        RedisCacheException ex =
                assertThrows(
                        RedisCacheException.class,
                        () -> adapter.put(
                                "123",
                                cliente,
                                Duration.ofMinutes(10)
                        )
                );

        assertEquals(
                "Erro ao serializar valor para cache Redis",
                ex.getMessage()
        );
    }

    @Test
    void shouldReturnObjectWhenCacheExists() throws Exception {

        Cliente cliente = new Cliente("teste", 42);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.get("redis:123"))
                .thenReturn("{\"nome\":\"teste\"}");

        when(objectMapper.readValue(
                "{\"nome\":\"teste\"}",
                Cliente.class))
                .thenReturn(cliente);

        Optional<Cliente> response =
                adapter.get(
                        "123",
                        Cliente.class
                );

        assertTrue(response.isPresent());
        assertEquals("teste", response.get().nome());
    }

    @Test
    void shouldReturnEmptyWhenCacheDoesNotExist() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.get("redis:123"))
                .thenReturn(null);

        Optional<Cliente> response =
                adapter.get(
                        "123",
                        Cliente.class
                );

        assertTrue(response.isEmpty());
    }

    @Test
    void shouldThrowRedisCacheExceptionWhenDeserializationFails() throws Exception {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.get("redis:123"))
                .thenReturn("json");

        when(objectMapper.readValue(
                anyString(),
                eq(Cliente.class)))
                .thenThrow(mock(JsonProcessingException.class));

        RedisCacheException ex =
                assertThrows(
                        RedisCacheException.class,
                        () -> adapter.get(
                                "123",
                                Cliente.class
                        )
                );

        assertEquals(
                "Erro ao desserializar valor do cache Redis",
                ex.getMessage()
        );
    }

    @Test
    void shouldEvictCache() {

        adapter.evict("123");

        verify(redisTemplate)
                .delete("redis:123");
    }

    @Test
    void shouldReturnTrueWhenKeyExists() {

        when(redisTemplate.hasKey("redis:123"))
                .thenReturn(true);

        boolean response = adapter.exists("123");

        assertTrue(response);
    }

    @Test
    void shouldReturnFalseWhenKeyDoesNotExist() {

        when(redisTemplate.hasKey("redis:123"))
                .thenReturn(false);

        boolean response = adapter.exists("123");

        assertFalse(response);
    }

    @Test
    void shouldReturnFalseWhenRedisReturnsNull() {

        when(redisTemplate.hasKey("redis:123"))
                .thenReturn(null);

        boolean response = adapter.exists("123");

        assertFalse(response);
    }
}