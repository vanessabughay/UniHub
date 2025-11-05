package com.unihub.backend.repository;

import com.unihub.backend.model.QuadroPlanejamento;
import com.unihub.backend.model.enums.QuadroStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuadroPlanejamentoRepository extends JpaRepository<QuadroPlanejamento, Long> {

    List<QuadroPlanejamento> findByUsuarioId(Long usuarioId);

    List<QuadroPlanejamento> findByUsuarioIdAndStatus(Long usuarioId, QuadroStatus status);

    List<QuadroPlanejamento> findByUsuarioIdAndTituloContainingIgnoreCase(Long usuarioId, String titulo);

    List<QuadroPlanejamento> findByUsuarioIdAndStatusAndTituloContainingIgnoreCase(Long usuarioId, QuadroStatus status, String titulo);

    Optional<QuadroPlanejamento> findByIdAndUsuarioId(Long id, Long usuarioId);

    @Query("""
        SELECT DISTINCT q FROM QuadroPlanejamento q
        LEFT JOIN q.contato contato
        LEFT JOIN q.grupo grupo
        LEFT JOIN grupo.membros membro
        LEFT JOIN q.colunas coluna
        LEFT JOIN coluna.tarefas tarefa
        LEFT JOIN tarefa.responsaveis responsavel
        WHERE q.usuario.id = :usuarioId
            OR (contato.idContato IS NOT NULL AND contato.idContato = :usuarioId)
            OR (membro.idContato IS NOT NULL AND membro.idContato = :usuarioId)
            OR (responsavel.idContato IS NOT NULL AND responsavel.idContato = :usuarioId)
        """)
    List<QuadroPlanejamento> findAllAccessibleByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("""
        SELECT DISTINCT q FROM QuadroPlanejamento q
        LEFT JOIN q.contato contato
        LEFT JOIN q.grupo grupo
        LEFT JOIN grupo.membros membro
        LEFT JOIN q.colunas coluna
        LEFT JOIN coluna.tarefas tarefa
        LEFT JOIN tarefa.responsaveis responsavel
        WHERE q.id = :quadroId AND (
            q.usuario.id = :usuarioId
            OR (contato.idContato IS NOT NULL AND contato.idContato = :usuarioId)
            OR (membro.idContato IS NOT NULL AND membro.idContato = :usuarioId)
            OR (responsavel.idContato IS NOT NULL AND responsavel.idContato = :usuarioId)
        )
        """)
    Optional<QuadroPlanejamento> findByIdAndUsuarioHasAccess(@Param("quadroId") Long quadroId, @Param("usuarioId") Long usuarioId);
}