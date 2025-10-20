package com.unihub.backend.repository;

import com.unihub.backend.model.Grupo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    List<Grupo> findByOwnerId(Long ownerId);
    Optional<Grupo> findByIdAndOwnerId(Long id, Long ownerId);
    boolean existsByIdAndOwnerId(Long id, Long ownerId);
    List<Grupo> findByNomeContainingIgnoreCaseAndOwnerId(String nome, Long ownerId);

    List<Grupo> findDistinctByMembros_IdContatoIn(Collection<Long> membroIds);

    Optional<Grupo> findDistinctByIdAndMembros_IdContatoIn(Long id, Collection<Long> membroIds);

    List<Grupo> findDistinctByNomeContainingIgnoreCaseAndMembros_IdContatoIn(String nome, Collection<Long> membroIds);

}

