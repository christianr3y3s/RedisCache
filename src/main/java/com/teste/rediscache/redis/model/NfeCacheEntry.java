package com.teste.rediscache.redis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NfeCacheEntry {

    private String chaveAcesso;

    private String cnpjEmitente;

    private String numeroNota;

    private String serie;

    private String xml;

    private Instant dataRecebimento;
}