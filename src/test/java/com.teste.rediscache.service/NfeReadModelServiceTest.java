package com.teste.rediscache.service;

import com.teste.rediscache.model.EventMessage;
import com.teste.rediscache.readmodel.entity.NfeReadModelEntity;
import com.teste.rediscache.readmodel.repository.NfeReadModelRepository;
import com.teste.rediscache.readmodel.service.NfeReadModelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class NfeReadModelServiceTest {

    @MockBean
    private NfeReadModelService readModelService;

    @Mock
    private NfeReadModelRepository readModelRepository;
    
    public NfeReadModelServiceTest() {
    }

    @BeforeEach
    void setUp() {
        readModelService = new NfeReadModelService(readModelRepository);
    }


    @Test
    @DisplayName("Deve criar novo Read Model quando a chave de acesso nao existir")
    void shouldCreateNewReadModelWhenChaveAcessoDoesNotExist() {
        // Given
        String correlationId = UUID.randomUUID().toString();
        String chaveAcesso = "35260800000000000000550010000000011000000001";
        EventMessage eventMessage = EventMessage.builder()
                .correlationId(correlationId)
                .eventType("NFE_RECEBIDA")
                .payload("{\"numero\": \"001\", \"serie\": \"1\"}")
                .timestamp(Instant.now()) // <-- Alterado aqui
                .build();

        // When
        NfeReadModelEntity savedEntity = readModelService.applyEvent(eventMessage, chaveAcesso, "RECEBIDA");

        // Then
        assertThat(savedEntity).isNotNull();
        assertThat(savedEntity.getChaveAcesso()).isEqualTo(chaveAcesso);
        assertThat(savedEntity.getCorrelationId()).isEqualTo(correlationId);
        assertThat(savedEntity.getUltimoEvento()).isEqualTo("NFE_RECEBIDA");
        assertThat(savedEntity.getStatus()).isEqualTo("RECEBIDA");
        assertThat(savedEntity.getDataAtualizacao()).isNotNull();

        Optional<NfeReadModelEntity> persistedEntity = readModelRepository.findById(chaveAcesso);
        assertThat(persistedEntity).isPresent();
    }

    @Test
    @DisplayName("Deve atualizar Read Model existente quando receber evento NFE_AUTORIZADA")
    void shouldUpdateExistingReadModelWhenNfeIsAutorizada() {
        // Given - Criação inicial
        String correlationId1 = UUID.randomUUID().toString();
        String chaveAcesso = "35260800000000000000550010000000011000000001";

        EventMessage eventInicial = EventMessage.builder()
                .correlationId(correlationId1)
                .eventType("NFE_RECEBIDA")
                .payload("{}")
                .timestamp(Instant.now()) // <-- Alterado aqui
                .build();

        readModelService.applyEvent(eventInicial, chaveAcesso, "PROCESSING");

        String correlationId2 = UUID.randomUUID().toString();
        EventMessage eventAutorizada = EventMessage.builder()
                .correlationId(correlationId2)
                .eventType("NFE_AUTORIZADA")
                .payload("{}")
                .timestamp(Instant.now()) // <-- Alterado aqui
                .build();

        // When
        NfeReadModelEntity updatedEntity = readModelService.applyEvent(eventAutorizada, chaveAcesso, "AUTORIZADA");

        // Then
        assertThat(updatedEntity.getChaveAcesso()).isEqualTo(chaveAcesso);
        assertThat(updatedEntity.getCorrelationId()).isEqualTo(correlationId2);
        assertThat(updatedEntity.getUltimoEvento()).isEqualTo("NFE_AUTORIZADA");
        assertThat(updatedEntity.getStatus()).isEqualTo("AUTORIZADA");
        assertThat(updatedEntity.getDataAutorizacao()).isNotNull();

        assertThat(readModelRepository.count()).isEqualTo(1);
    }
}