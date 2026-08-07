package com.teste.rediscache.eventstore.service;

import com.teste.rediscache.eventstore.entity.EventStoreEntity;
import com.teste.rediscache.eventstore.exceptions.EventStoreException;
import com.teste.rediscache.eventstore.repository.EventStoreRepository;
import com.teste.rediscache.interfaces.EventStoreClient;
import com.teste.rediscache.model.EventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventStoreService implements EventStoreClient {

    private final EventStoreRepository repository;

    @Override
    public void save(EventMessage event) {

        try {

            EventStoreEntity entity =
                    new EventStoreEntity();
            entity.setCorrelationId(
                    event.getCorrelationId()
            );
            entity.setEventType(
                    event.getEventType()
            );
            entity.setPayload(
                    event.getPayload()
            );
            entity.setCreatedAt(
                    Instant.now()
            );
            repository.save(entity);
            log.info(
                    "[EVENT_STORE] correlationId={} eventType={}",
                    event.getCorrelationId(),
                    event.getEventType()
            );
        } catch (Exception ex) {
            log.error(
                    "[EVENT_STORE] correlationId={} eventType={}",
                    event.getCorrelationId(), event.getEventType(), ex );
            throw new EventStoreException( "Erro ao persistir evento", ex );
        }
    }
}