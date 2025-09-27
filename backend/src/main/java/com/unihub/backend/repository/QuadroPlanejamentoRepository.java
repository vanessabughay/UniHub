package com.unihub.backend.repository;

import com.unihub.backend.model.QuadroPlanejamento;
import com.unihub.backend.model.enums.QuadroStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuadroPlanejamentoRepository extends JpaRepository<QuadroPlanejamento, Long> {
    List<QuadroPlanejamento> findByUsuarioId(Long usuarioId);
    List<QuadroPlanejamento> findByUsuarioIdAndStatus(Long usuarioId, QuadroStatus status);
    List<QuadroPlanejamento> findByUsuarioIdAndTituloContainingIgnoreCase(Long usuarioId, String titulo);
    List<QuadroPlanejamento> findByUsuarioIdAndStatusAndTituloContainingIgnoreCase(Long usuarioId, QuadroStatus status, String titulo);
    Optional<QuadroPlanejamento> findByIdAndUsuarioId(Long id, Long usuarioId);
}