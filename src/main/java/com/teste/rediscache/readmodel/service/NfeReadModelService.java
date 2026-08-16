package com.teste.rediscache.readmodel.service;

import com.teste.rediscache.model.EventMessage;
import com.teste.rediscache.readmodel.entity.NfeReadModelEntity;
import com.teste.rediscache.readmodel.repository.NfeReadModelRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NfeReadModelService {

    private static final Logger log = LoggerFactory.getLogger(NfeReadModelService.class);

    private final NfeReadModelRepository readModelRepository;

    @Transactional
    public NfeReadModelEntity applyEvent(EventMessage eventMessage, String chaveAcesso, String status) {
        log.info("Materializing read model for eventType: {} | correlationId: {}",
                eventMessage.getEventType(), eventMessage.getCorrelationId());

        NfeReadModelEntity readModel = readModelRepository.findById(chaveAcesso)
                .orElseGet(() -> NfeReadModelEntity.builder()
                        .chaveAcesso(chaveAcesso)
                        .dataEmissao(LocalDateTime.now())
                        .build());

        readModel.setChaveAcesso(chaveAcesso);
        readModel.setUltimoEvento(eventMessage.getEventType());
        readModel.setCorrelationId(eventMessage.getCorrelationId());
        readModel.setStatus(status);
        readModel.setDataAtualizacao(LocalDateTime.now());

        if ("NFE_AUTORIZADA".equalsIgnoreCase(eventMessage.getEventType())) {
            readModel.setDataAutorizacao(LocalDateTime.now());
        }

        return readModelRepository.saveAndFlush(readModel);
    }
}