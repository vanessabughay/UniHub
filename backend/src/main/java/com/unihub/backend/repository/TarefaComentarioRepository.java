package com.unihub.backend.repository;

import com.unihub.backend.model.TarefaComentario;
import com.unihub.backend.model.TarefaPlanejamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TarefaComentarioRepository extends JpaRepository<TarefaComentario, Long> {

    List<TarefaComentario> findByTarefaOrderByDataCriacaoDesc(TarefaPlanejamento tarefa);

    Optional<TarefaComentario> findByIdAndTarefaId(Long id, Long tarefaId);

    void deleteByAutorId(Long autorId);
}