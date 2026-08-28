package com.teste.rediscache.readmodel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "NFE_READMODEL")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NfeReadModelEntity {

    @Id
    @Column(name = "chave_acesso", nullable = false, length = 44)
    private String chaveAcesso;

    @Column(name = "numero")
    private String numero;

    @Column(name = "serie")
    private String serie;

    @Column(name = "status")
    private String status;

    @Column(name = "ultimo_evento")
    private String ultimoEvento;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "data_emissao")
    private LocalDateTime dataEmissao;

    @Column(name = "data_autorizacao")
    private LocalDateTime dataAutorizacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
}