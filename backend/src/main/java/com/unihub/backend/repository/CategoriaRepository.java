package com.unihub.backend.repository;

import com.unihub.backend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByUsuarioId(Long usuarioId);
    Optional<Categoria> findByNomeAndUsuarioId(String nome, Long usuarioId);
}