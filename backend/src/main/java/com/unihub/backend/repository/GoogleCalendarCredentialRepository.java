package com.unihub.backend.repository;

import com.unihub.backend.model.GoogleCalendarCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoogleCalendarCredentialRepository extends JpaRepository<GoogleCalendarCredential, Long> {
    Optional<GoogleCalendarCredential> findByUsuarioId(Long usuarioId);
    boolean existsByUsuarioId(Long usuarioId);
    void deleteByUsuarioId(Long usuarioId);
}