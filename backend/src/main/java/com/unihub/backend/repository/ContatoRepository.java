package com.unihub.backend.repository;

import com.unihub.backend.model.Contato;

import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

import java.util.List;

public interface ContatoRepository extends JpaRepository<Contato, Long> {
    List<Contato> findByOwnerId(Long ownerId);
    Optional<Contato> findByIdAndOwnerId(Long id, Long ownerId);
    boolean existsByIdAndOwnerId(Long id, Long ownerId);
    List<Contato> findByOwnerIdAndIdIn(Long ownerId, List<Long> ids);
    List<Contato> findByEmailIgnoreCaseAndPendenteTrue(String email);
    List<Contato> findByEmailIgnoreCase(String email);
    List<Contato> findByIdContato(Long idContato);
    Optional<Contato> findByOwnerIdAndIdContato(Long ownerId, Long idContato);

    Optional<Contato> findByOwnerIdAndEmailIgnoreCase(Long ownerId, String email);
    Optional<Contato> findByOwnerIdAndEmail(Long ownerId, String email);

}


