package com.unihub.backend.repository;

import com.unihub.backend.model.TarefaPlanejamento;
import com.unihub.backend.model.enums.TarefaStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TarefaPlanejamentoRepository extends JpaRepository<TarefaPlanejamento, Long> {

    List<TarefaPlanejamento> findByColunaIdOrderByDataPrazoAsc(Long colunaId);

    Optional<TarefaPlanejamento> findByIdAndColunaQuadroId(Long id, Long quadroId);

     Optional<TarefaPlanejamento> findByIdAndColunaId(Long id, Long colunaId);

    long countByColunaQuadroIdAndStatus(Long quadroId, TarefaStatus status);
}