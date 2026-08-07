package com.teste.rediscache.eventstore.exceptions;

public class RedisCacheException extends RuntimeException {
    public RedisCacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
