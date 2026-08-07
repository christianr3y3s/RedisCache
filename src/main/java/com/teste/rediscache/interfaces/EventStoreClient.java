package com.teste.rediscache.interfaces;

import com.teste.rediscache.model.EventMessage;

public interface EventStoreClient {
    void save(EventMessage event);
}