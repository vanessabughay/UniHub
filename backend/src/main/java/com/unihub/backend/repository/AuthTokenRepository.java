package com.unihub.backend.repository;

import com.unihub.backend.model.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    Optional<AuthToken> findByToken(String token);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByToken(String token);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByUsuarioId(Long usuarioId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByExpiraEmBefore(LocalDateTime limite);

    Optional<AuthToken> findFirstByUsuarioIdAndExpiraEmAfterOrderByExpiraEmDesc(Long usuarioId,
                                                                               LocalDateTime agora);

    List<AuthToken> findByExpiraEmAfter(LocalDateTime agora);
}