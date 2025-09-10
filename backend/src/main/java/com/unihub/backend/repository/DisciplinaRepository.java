package com.unihub.backend.repository;

import com.unihub.backend.model.Disciplina;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;


public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {
    List<Disciplina> findByNomeContainingIgnoreCase(String nome);
    List<Disciplina> findByUsuarioId(Long usuarioId);
    List<Disciplina> findByUsuarioIdAndNomeContainingIgnoreCase(Long usuarioId, String nome);
    Optional<Disciplina> findByIdAndUsuarioId(Long id, Long usuarioId);
}
