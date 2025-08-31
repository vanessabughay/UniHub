package com.unihub.backend.repository;

import com.unihub.backend.model.Ausencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AusenciaRepository extends JpaRepository<Ausencia, Long> {
    List<Ausencia> findByUsuarioId(Long usuarioId);
    Optional<Ausencia> findByIdAndUsuarioId(Long id, Long usuarioId);
}