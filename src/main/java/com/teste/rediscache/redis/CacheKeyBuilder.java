package com.teste.rediscache.redis;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CacheKeyBuilder {

    private final String keyPrefix;
    private final String namespace;

    public String build(String key) {
        return "%s:%s:%s"
                .formatted(
                        keyPrefix,
                        namespace,
                        key
                );
    }
}
