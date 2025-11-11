package com.unihub.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    public NotificacaoService(NotificacaoRepository notificacaoRepository,
                              UsuarioRepository usuarioRepository,
                              ObjectMapper objectMapper) {
        this.notificacaoRepository = notificacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.objectMapper = objectMapper;
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
        String categoria = Optional.ofNullable(request.getCategoria())
                .map(String::trim)  
                .filter(value -> !value.isEmpty())
                .orElse(null);
        String titulo = Optional.ofNullable(request.getTitulo())
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .orElse(null);
        Long referenciaId = request.getReferenciaId();
        boolean interacaoPendente = Boolean.TRUE.equals(request.getInteracaoPendente());
        String metadataJson = serializeMetadata(request.getMetadata());

        Optional<Notificacao> existente = Optional.empty();

         if (referenciaId != null) {
            if (categoria != null) {
                existente = notificacaoRepository
                        .findByUsuarioIdAndTipoAndCategoriaAndReferenciaId(usuarioId, tipo, categoria, referenciaId);
            }

            if (existente.isEmpty()) {
                existente = notificacaoRepository
                        .findByUsuarioIdAndTipoAndReferenciaId(usuarioId, tipo, referenciaId);
            }
        }

        if (existente.isEmpty()) {
            existente = notificacaoRepository
                    .findByUsuarioIdAndTipoAndMensagemAndCriadaEm(usuarioId, tipo, mensagem, criadaEm);
        }

        Notificacao notificacao = existente.orElseGet(Notificacao::new);
        boolean novaNotificacao = notificacao.getId() == null;
        notificacao.setUsuario(usuario);
        notificacao.setConvite(null);
        notificacao.setTitulo(titulo);
        notificacao.setMensagem(mensagem);
        notificacao.setTipo(tipo);
        notificacao.setCategoria(categoria);
        notificacao.setReferenciaId(referenciaId);
        if (interacaoPendente) {
            notificacao.setLida(false);
        }
        notificacao.setInteracaoPendente(interacaoPendente);
        if (metadataJson != null) {
            notificacao.setMetadataJson(metadataJson);
        }
        if (novaNotificacao) {
            notificacao.setCriadaEm(criadaEm);
            notificacao.setLida(false);
        }
        notificacao.setAtualizadaEm(LocalDateTime.now());

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
    
    private String serializeMetadata(java.util.Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException exception) {
            return null;
        }
    }
}