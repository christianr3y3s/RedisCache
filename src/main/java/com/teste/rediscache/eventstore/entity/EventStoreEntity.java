package com.teste.rediscache.eventstore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "EVENT_STORE")
@Getter
@Setter
public class EventStoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String correlationId;

    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private Instant createdAt;
}