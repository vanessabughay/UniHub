package com.unihub.backend.repository;

import com.unihub.backend.model.ColunaPlanejamento;
import com.unihub.backend.model.enums.EstadoPlanejamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ColunaPlanejamentoRepository extends JpaRepository<ColunaPlanejamento, Long> {

    List<ColunaPlanejamento> findByQuadroIdOrderByOrdemAsc(Long quadroId);

    List<ColunaPlanejamento> findByQuadroIdAndEstadoOrderByOrdemAsc(Long quadroId, EstadoPlanejamento estado);

    Optional<ColunaPlanejamento> findTopByQuadroIdOrderByOrdemDesc(Long quadroId);

    Optional<ColunaPlanejamento> findByIdAndQuadroId(Long id, Long quadroId);

    @Query("SELECT COALESCE(MAX(c.ordem), 0) FROM ColunaPlanejamento c WHERE c.quadro.id = :quadroId")
    Integer findMaxOrdemByQuadroId(@Param("quadroId") Long quadroId);
}