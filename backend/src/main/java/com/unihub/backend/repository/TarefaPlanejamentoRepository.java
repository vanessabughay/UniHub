package com.unihub.backend.repository;

import com.unihub.backend.model.TarefaPlanejamento;
import com.unihub.backend.model.enums.TarefaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TarefaPlanejamentoRepository extends JpaRepository<TarefaPlanejamento, Long> {

    List<TarefaPlanejamento> findByColunaIdOrderByDataPrazoAsc(Long colunaId);

    Optional<TarefaPlanejamento> findByIdAndColunaQuadroId(Long id, Long quadroId);

    Optional<TarefaPlanejamento> findByIdAndColunaId(Long id, Long colunaId);

    long countByColunaQuadroIdAndStatus(Long quadroId, TarefaStatus status);

    @Query("SELECT t FROM TarefaPlanejamento t JOIN t.responsaveis r " +
           "WHERE r.id = :contatoId " + // Espera contatoId
           "AND t.status != 'CONCLUIDA' " +
           "AND t.dataPrazo BETWEEN :dataInicio AND :dataFim " +
           "ORDER BY t.dataPrazo ASC")
    List<TarefaPlanejamento> findProximasTarefasPorResponsavel(
            @Param("contatoId") Long contatoId, // Espera contatoId
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );
}