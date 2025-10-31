package com.unihub.backend.repository;

import com.unihub.backend.model.TarefaNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarefaNotificacaoRepository extends JpaRepository<TarefaNotificacao, Long> {
    
    boolean existsByTarefaIdAndUsuarioId(Long tarefaId, Long usuarioId);

    void deleteByTarefaIdAndUsuarioId(Long tarefaId, Long usuarioId);
}