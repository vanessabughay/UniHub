package com.unihub.backend.repository;

import com.unihub.backend.model.Contato;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import java.util.List;

public interface ContatoRepository extends JpaRepository<Contato, Long> {
    List<Contato> findByOwnerId(Long ownerId);
    Optional<Contato> findByIdAndOwnerId(Long id, Long ownerId);
    boolean existsByIdAndOwnerId(Long id, Long ownerId);
    List<Contato> findByOwnerIdAndIdIn(Long ownerId, List<Long> ids);
    List<Contato> findByEmailIgnoreCaseAndPendenteTrue(String email);
    Optional<Contato> findByOwnerIdAndEmailIgnoreCase(Long ownerId, String email);

    @Modifying
    @Query(value = "UPDATE contato SET id = :novoId WHERE id = :idAntigo", nativeQuery = true)
    int atualizarId(@Param("idAntigo") Long idAntigo, @Param("novoId") Long novoId);
}


