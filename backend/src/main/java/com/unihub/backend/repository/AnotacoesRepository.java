package com.unihub.backend.repository;

import com.unihub.backend.model.Anotacoes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnotacoesRepository extends JpaRepository<Anotacoes, Long> {
    Page<Anotacoes> findAllByDisciplinaId(Long disciplinaId, Pageable pageable);
    Optional<Anotacoes> findByIdAndDisciplinaId(Long id, Long disciplinaId);
}