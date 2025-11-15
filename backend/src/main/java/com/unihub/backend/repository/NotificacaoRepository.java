package com.unihub.backend.repository;

import com.unihub.backend.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByUsuarioIdOrderByCriadaEmDesc(Long usuarioId);

    List<Notificacao> findByUsuarioIdOrderByAtualizadaEmDesc(Long usuarioId);

    List<Notificacao> findByConviteIdAndUsuarioId(Long conviteId, Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);

    Optional<Notificacao> findByUsuarioIdAndTipoAndMensagemAndCriadaEm(Long usuarioId,
                                                                       String tipo,
                                                                       String mensagem,
                                                                       LocalDateTime criadaEm);
                                                            
    Optional<Notificacao> findByUsuarioIdAndTipoAndReferenciaId(Long usuarioId,
                                                                String tipo,
                                                                Long referenciaId);

    Optional<Notificacao> findByUsuarioIdAndTipoAndCategoriaAndReferenciaId(Long usuarioId,
                                                                            String tipo,
                                                                            String categoria,
                                                                            Long referenciaId);
}