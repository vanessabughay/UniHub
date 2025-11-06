package com.unihub.backend.repository;

import com.unihub.backend.model.TarefaPlanejamento;
import com.unihub.backend.model.enums.TarefaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TarefaPlanejamentoRepository extends JpaRepository<TarefaPlanejamento, Long> {

    List<TarefaPlanejamento> findByColunaIdOrderByDataPrazoAsc(Long colunaId);

    Optional<TarefaPlanejamento> findByIdAndColunaQuadroId(Long id, Long quadroId);

    Optional<TarefaPlanejamento> findByIdAndColunaId(Long id, Long colunaId);

    long countByColunaQuadroIdAndStatus(Long quadroId, TarefaStatus status);



    List<TarefaPlanejamento> findDistinctByResponsaveis_IdContato(Long idContato);

    List<TarefaPlanejamento> findDistinctByResponsaveis_Id(Long contatoId);

    @Query("SELECT t FROM TarefaPlanejamento t JOIN t.responsaveis r " +
           "WHERE r.idContato = :usuarioId " +
           "AND t.status <> 'CONCLUIDA' " +
           "AND t.dataPrazo BETWEEN :dataInicio AND :dataFim " +
           "ORDER BY t.dataPrazo ASC")
    List<TarefaPlanejamento> findProximasTarefasPorResponsavel(
            @Param("usuarioId") Long usuarioId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    

    @Query("""
            SELECT DISTINCT t
            FROM TarefaPlanejamento t
            JOIN t.coluna c
            JOIN c.quadro q
            LEFT JOIN t.notificacoes notificacao
            LEFT JOIN q.grupo grupo
            LEFT JOIN grupo.membros membro
            LEFT JOIN q.contato contato
            LEFT JOIN t.responsaveis responsavel
            WHERE t.status <> 'CONCLUIDA'
              AND t.dataPrazo IS NOT NULL
              AND t.dataPrazo BETWEEN :dataInicio AND :dataFim
              AND (
                    q.usuario.id = :usuarioId
                 OR (responsavel.idContato IS NOT NULL AND responsavel.idContato = :usuarioId)
                 OR (contato.idContato IS NOT NULL AND contato.idContato = :usuarioId)
                 OR (membro.idContato IS NOT NULL AND membro.idContato = :usuarioId)
                 OR (notificacao.usuario.id IS NOT NULL AND notificacao.usuario.id = :usuarioId)
              )
            ORDER BY t.dataPrazo ASC
            """)
    List<TarefaPlanejamento> findProximasTarefasPorParticipante(
            @Param("usuarioId") Long usuarioId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );
}
