package com.teste.rediscache.eventstore.listener;

import com.teste.rediscache.interfaces.EventStoreClient;
import com.teste.rediscache.model.EventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventStoreListener {

    private final EventStoreClient eventStoreClient;

    @KafkaListener(
            topics = "${event.store.topic}"
    )
    public void consume(EventMessage event) {

        log.info(
                "[EVENT_RECEIVED] correlationId={}",
                event.getCorrelationId()
        );

        eventStoreClient.save(event);
    }
}