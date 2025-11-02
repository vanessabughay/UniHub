package com.unihub.backend.repository;

import com.unihub.backend.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByUsuarioIdOrderByCriadaEmDesc(Long usuarioId);

    List<Notificacao> findByConviteIdAndUsuarioId(Long conviteId, Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);
}