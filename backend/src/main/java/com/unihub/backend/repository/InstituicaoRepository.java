package com.unihub.backend.repository;

import com.unihub.backend.model.Instituicao;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstituicaoRepository extends JpaRepository<Instituicao, Long> {
    // Este método foi removido pois a funcionalidade foi unificada no método abaixo.
    // List<Instituicao> findByUsuarioId(Long usuarioId);
    @Query("SELECT i FROM Instituicao i WHERE i.usuario.id = :usuarioId AND (:nome IS NULL OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) ORDER BY LOWER(i.nome)")    List<Instituicao> findByUsuarioIdAndNomeContainingIgnoreCase(Long usuarioId, String nome);
    Optional<Instituicao> findByIdAndUsuarioId(Long id, Long usuarioId);
    List<Instituicao> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);
    List<Instituicao> findByUsuarioIdOrderByNomeAsc(Long usuarioId);
 
}