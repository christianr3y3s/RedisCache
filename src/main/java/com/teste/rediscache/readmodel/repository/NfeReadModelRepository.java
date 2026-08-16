package com.teste.rediscache.readmodel.repository;

import com.teste.rediscache.readmodel.entity.NfeReadModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NfeReadModelRepository extends JpaRepository<NfeReadModelEntity, String> {

    Optional<NfeReadModelEntity> findByCorrelationId(String correlationId);

    Optional<NfeReadModelEntity> findByChaveAcesso(String chaveAcesso);
}