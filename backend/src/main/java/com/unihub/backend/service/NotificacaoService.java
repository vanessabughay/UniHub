package com.unihub.backend.service;

import com.unihub.backend.dto.compartilhamento.NotificacaoResponse;
import com.unihub.backend.dto.notificacoes.NotificacaoLogRequest;
import com.unihub.backend.exceptions.ResourceNotFoundException;
import com.unihub.backend.model.Notificacao;
import com.unihub.backend.model.Usuario;
import com.unihub.backend.repository.NotificacaoRepository;
import com.unihub.backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class NotificacaoService {

    private static final String DEFAULT_TIPO = "APP_NOTIFICACAO";

    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository,
                              UsuarioRepository usuarioRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public NotificacaoResponse registrarNotificacao(Long usuarioId, NotificacaoLogRequest request) {
        if (request == null || request.getMensagem() == null || request.getMensagem().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mensagem da notificação é obrigatória");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        LocalDateTime criadaEm = converterTimestamp(request.getTimestamp());
        String mensagem = request.getMensagem().trim();
        String tipo = Optional.ofNullable(request.getTipo())
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .orElse(DEFAULT_TIPO);
        String titulo = Optional.ofNullable(request.getTitulo())
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .orElse(null);

        Optional<Notificacao> existente = notificacaoRepository
                .findByUsuarioIdAndTipoAndMensagemAndCriadaEm(usuarioId, tipo, mensagem, criadaEm);

        Notificacao notificacao = existente.orElseGet(Notificacao::new);
        notificacao.setUsuario(usuario);
        notificacao.setConvite(null);
        notificacao.setTitulo(titulo);
        notificacao.setMensagem(mensagem);
        notificacao.setTipo(tipo);
        notificacao.setCriadaEm(criadaEm);
        notificacao.setLida(false);

        Notificacao salvo = notificacaoRepository.save(notificacao);
        return NotificacaoResponse.fromEntity(salvo);
    }

    private LocalDateTime converterTimestamp(Long timestamp) {
        if (timestamp == null || timestamp <= 0) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        } catch (Exception ex) {
            return LocalDateTime.now();
        }
    }
}