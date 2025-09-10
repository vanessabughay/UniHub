package com.unihub.backend.repository;

import com.unihub.backend.model.Grupo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    List<Grupo> findByNomeContainingIgnoreCase(String nome);

}

