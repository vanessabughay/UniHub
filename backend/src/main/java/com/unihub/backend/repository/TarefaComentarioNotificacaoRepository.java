package com.unihub.backend.repository;

import com.unihub.backend.model.TarefaComentarioNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarefaComentarioNotificacaoRepository extends JpaRepository<TarefaComentarioNotificacao, Long> {

    boolean existsByTarefaIdAndUsuarioId(Long tarefaId, Long usuarioId);

    void deleteByTarefaIdAndUsuarioId(Long tarefaId, Long usuarioId);
}