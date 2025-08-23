package com.unihub.backend.repository;

import com.unihub.backend.model.Instituicao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstituicaoRepository extends JpaRepository<Instituicao, Long> {
    List<Instituicao> findByNomeContainingIgnoreCase(String nome);
}