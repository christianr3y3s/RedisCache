package com.teste.rediscache.eventstore.repository;

import com.teste.rediscache.eventstore.entity.EventStoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventStoreRepository extends JpaRepository<EventStoreEntity, String> {
}