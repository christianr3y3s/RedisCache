package com.teste.rediscache.readmodel.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

@Entity
@Table(name = "NFE_READMODEL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NfeReadModelEntity implements Persistable<String> {

    @Id
    @Column(name = "chave_acesso", nullable = false, length = 44)
    private String chaveAcesso;

    private String correlationId;
    private String ultimoEvento;
    private String status;
    private String numero;
    private String serie;
    private LocalDateTime dataEmissao;
    private LocalDateTime dataAtualizacao;
    private LocalDateTime dataAutorizacao;

    @Transient
    @Builder.Default
    private boolean isNewRecord = true;

    @Override
    public String getId() {
        return this.chaveAcesso;
    }

    @Override
    public boolean isNew() {
        return this.isNewRecord;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNewRecord = false;
    }
}