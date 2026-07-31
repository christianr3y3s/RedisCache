package com.teste.rediscache.redis.listeners;

import com.teste.rediscache.redis.services.NfeCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NfeListener {

    private final NfeCacheService service;

    @KafkaListener(
            topics = "${nfe.kafka.topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumir(String mensagem) {

        log.info(
                "[NFE_RECEIVED] payload recebido"
        );

        service.processar(mensagem);
    }
}