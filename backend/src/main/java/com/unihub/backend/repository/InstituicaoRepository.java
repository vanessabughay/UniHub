package com.unihub.backend.repository;

import com.unihub.backend.model.Instituicao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstituicaoRepository extends JpaRepository<Instituicao, Long> {
    List<Instituicao> findByNomeContainingIgnoreCase(String nome);
    List<Instituicao> findByUsuarioId(Long usuarioId);
    List<Instituicao> findByUsuarioIdAndNomeContainingIgnoreCase(Long usuarioId, String nome);
    Optional<Instituicao> findByIdAndUsuarioId(Long id, Long usuarioId);
}