package com.teste.rediscache.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventMessage {

    private String correlationId;

    private String eventType;

    private String payload;

    private Instant timestamp;
}
