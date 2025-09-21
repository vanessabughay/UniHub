package com.unihub.backend.repository;

import com.unihub.backend.model.Avaliacao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    List<Avaliacao> findByDescricaoContainingIgnoreCase(String descricao);
}