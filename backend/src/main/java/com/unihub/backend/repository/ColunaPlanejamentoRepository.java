package com.unihub.backend.repository;

import com.unihub.backend.model.ColunaPlanejamento;
import com.unihub.backend.model.enums.EstadoPlanejamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ColunaPlanejamentoRepository extends JpaRepository<ColunaPlanejamento, Long> {

    List<ColunaPlanejamento> findByQuadroIdOrderByDescricaoAsc(Long quadroId);

    List<ColunaPlanejamento> findByQuadroIdAndEstadoOrderByDescricaoAsc(Long quadroId, EstadoPlanejamento estado);
    
    Optional<ColunaPlanejamento> findByIdAndQuadroId(Long id, Long quadroId);
}