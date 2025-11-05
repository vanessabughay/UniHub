package com.unihub.backend.repository;

import com.unihub.backend.model.NotificacaoConfiguracao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificacaoConfiguracaoRepository extends JpaRepository<NotificacaoConfiguracao, Long> {

    @EntityGraph(attributePaths = "antecedencias")
    Optional<NotificacaoConfiguracao> findByUsuarioId(Long usuarioId);
}