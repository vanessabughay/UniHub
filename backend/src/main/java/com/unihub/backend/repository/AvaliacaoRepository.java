package com.unihub.backend.repository;

import com.unihub.backend.model.Avaliacao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;


import java.util.List;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    List<Avaliacao> findByDescricaoContainingIgnoreCase(String descricao);

    // carrega a disciplina junto em listagens
    @EntityGraph(attributePaths = {"disciplina"})
    List<Avaliacao> findAllBy();

    @EntityGraph(attributePaths = {"disciplina"})   //usado pelo listarPorDisciplina
    List<Avaliacao> findByDisciplinaId(Long disciplinaId);

    //busca com descrição já carregando disciplina
    @EntityGraph(attributePaths = {"disciplina"})
    List<Avaliacao> findByDescricaoContainingIgnoreCaseAndDisciplinaIsNotNull(String descricao);



}






