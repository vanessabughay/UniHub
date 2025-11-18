package com.unihub.backend.service;

import com.unihub.backend.dto.AvaliacaoRequest;
import com.unihub.backend.dto.ContatoRef;
import com.unihub.backend.model.*;
import com.unihub.backend.model.enums.Modalidade;
import com.unihub.backend.repository.*;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final ContatoRepository contatoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final GoogleCalendarSyncService googleCalendarSyncService;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository,
                            ContatoRepository contatoRepository,
                            DisciplinaRepository disciplinaRepository,
                            GoogleCalendarSyncService googleCalendarSyncService) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.contatoRepository = contatoRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.googleCalendarSyncService = googleCalendarSyncService;
    }

    @Transactional(readOnly = true)
    public List<Avaliacao> listarTodas(Long usuarioId) {
        validarUsuario(usuarioId);
        return avaliacaoRepository.findByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public Avaliacao buscarPorId(Long id, Long usuarioId) {
        validarUsuario(usuarioId);
        return avaliacaoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Avaliação não encontrada: " + id));
    }

    @Transactional
    public Long criar(AvaliacaoRequest req, Long usuarioId) {
        validarUsuario(usuarioId);
        Avaliacao a = new Avaliacao();
        aplicar(req, a, usuarioId);
        Avaliacao salvo = avaliacaoRepository.save(a);
        googleCalendarSyncService.syncEvaluation(salvo.getId(), usuarioId);
        return salvo.getId();
    }

    @Transactional
    public boolean atualizar(Long id, AvaliacaoRequest req, Long usuarioId) {
        validarUsuario(usuarioId);
        var opt = avaliacaoRepository.findByIdAndUsuarioId(id, usuarioId);
        if (opt.isEmpty()) {
            return false;
        }
        Avaliacao a = opt.get();
        aplicar(req, a, usuarioId);
        avaliacaoRepository.save(a);
        googleCalendarSyncService.syncEvaluation(a.getId(), usuarioId);
        return true;
    }

    private void aplicar(AvaliacaoRequest req, Avaliacao a, Long usuarioId) {
        a.setDescricao(req.descricao());
        a.setTipoAvaliacao(req.tipoAvaliacao());
        a.setModalidade(req.modalidade());
        a.setPrioridade(req.prioridade());
        a.setReceberNotificacoes(Boolean.TRUE.equals(req.receberNotificacoes()));
        a.setEstado(req.estado());
        a.setDificuldade(req.dificuldade());
        a.setNota(req.nota());
        a.setPeso(req.peso());
        a.setUsuario(referenciaUsuario(usuarioId));

        a.setDataEntrega(parseDataEntrega(req.dataEntrega()));


        // Disciplina (apenas id no request)
        Long discId = (req.disciplina() != null ? req.disciplina().id() : null);
        if (discId == null) throw new IllegalArgumentException("Disciplina é obrigatória");
        Disciplina disc = disciplinaRepository.findByIdAndUsuarioId(discId, usuarioId)
            .orElseThrow(() -> new EntityNotFoundException("Disciplina não encontrada: " + discId));
        a.setDisciplina(disc);

        // Integrantes (enviar/receber só ids)
        a.getIntegrantes().clear();
        if (a.getModalidade() == Modalidade.EM_GRUPO) {
            a.getIntegrantes().addAll(carregarIntegrantes(req.integrantes(), usuarioId));
        }
    }

    @Transactional
    public void excluir(Long id, Long usuarioId) {
        Avaliacao a = buscarPorId(id, usuarioId);
         googleCalendarSyncService.removeFromCalendar(id, usuarioId);
        avaliacaoRepository.delete(a);
    }

    @Transactional(readOnly = true)
    public List<Avaliacao> buscarPorNome(String descricao, Long usuarioId) {
        validarUsuario(usuarioId);
        return avaliacaoRepository.findByUsuarioIdAndDescricaoContainingIgnoreCaseAndDisciplinaIsNotNull(usuarioId, descricao);
    }

    @Transactional(readOnly = true)
    public List<Avaliacao> listarPorDisciplina(Long disciplinaId, Long usuarioId) {
        validarUsuario(usuarioId);
        return avaliacaoRepository.findByUsuarioIdAndDisciplinaId(usuarioId, disciplinaId);
    }

    private void validarUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("Usuário autenticado é obrigatório");
        }
    }

    private LocalDateTime parseDataEntrega(String data) {
        if (data == null || data.isBlank()) {
            return null;
        }

        String raw = data.trim();
        if (raw.contains("T")) {
            if (raw.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}$")) {
                raw = raw + ":00";
            }
            return LocalDateTime.parse(raw);
        }
        return LocalDate.parse(raw).atStartOfDay();
    }

    private List<Contato> carregarIntegrantes(List<ContatoRef> integrantes, Long usuarioId) {
        if (integrantes == null || integrantes.isEmpty()) {
            return List.of();
        }

        List<Long> ids = integrantes.stream()
                .map(ContatoRef::id)
                .toList();

        if (ids.isEmpty() || ids.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Todos os contatos devem possuir um ID ao vincular a uma avaliação.");
        }

        List<Contato> contatos = contatoRepository.findByOwnerIdAndIdIn(usuarioId, ids);
        Set<Long> solicitados = new java.util.HashSet<>(ids);
        Set<Long> encontrados = contatos.stream()
                .map(Contato::getId)
                .collect(java.util.stream.Collectors.toSet());

        if (!encontrados.containsAll(solicitados)) {
            throw new EntityNotFoundException("Alguns contatos informados não pertencem ao usuário autenticado.");
        }

        return contatos;
    }


    private Usuario referenciaUsuario(Long usuarioId) {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        return usuario;
    }
}
