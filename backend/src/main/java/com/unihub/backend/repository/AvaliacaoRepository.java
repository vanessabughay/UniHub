package com.unihub.backend.repository;

import com.unihub.backend.model.Avaliacao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;


import java.util.List;
import java.util.Optional;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    List<Avaliacao> findByDescricaoContainingIgnoreCase(String descricao);

    
    @EntityGraph(attributePaths = {"disciplina"})
    List<Avaliacao> findByUsuarioId(Long usuarioId);

    @EntityGraph(attributePaths = {"disciplina"})
    List<Avaliacao> findByUsuarioIdAndDisciplinaId(Long usuarioId, Long disciplinaId);

    Optional<Avaliacao> findByIdAndUsuarioId(Long id, Long usuarioId);

    @EntityGraph(attributePaths = {"disciplina"})
    List<Avaliacao> findByUsuarioIdAndDescricaoContainingIgnoreCaseAndDisciplinaIsNotNull(Long usuarioId, String descricao);

}






