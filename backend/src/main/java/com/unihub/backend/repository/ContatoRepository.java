package com.unihub.backend.repository;

import com.unihub.backend.model.Contato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContatoRepository extends JpaRepository<Contato, Long> {

    List<Contato> findByProprietarioId(Long proprietarioId);

    Optional<Contato> findByProprietarioIdAndContatoId(Long proprietarioId, Long contatoId);

    boolean existsByProprietarioIdAndContatoId(Long proprietarioId, Long contatoId);
}
