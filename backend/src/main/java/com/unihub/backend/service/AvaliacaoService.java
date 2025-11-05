package com.unihub.backend.service;

import com.unihub.backend.dto.AvaliacaoRequest;   // <- DTO
import com.unihub.backend.dto.ContatoRef;         // <- se precisar
import com.unihub.backend.model.*;
import com.unihub.backend.model.enums.Modalidade;
import com.unihub.backend.repository.*;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
        if (opt.isEmpty()) return false;
        Avaliacao a = opt.get();
        aplicar(req, a, usuarioId);
        avaliacaoRepository.save(a);
        googleCalendarSyncService.syncEvaluation(a.getId(), usuarioId);
        return true;
    }

    private void aplicar(AvaliacaoRequest req, Avaliacao a, Long usuarioId) {
        validarUsuario(usuarioId);
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

        if (req.dataEntrega() == null || req.dataEntrega().isBlank()) {
            a.setDataEntrega(null);
        } else {
            String raw = req.dataEntrega().trim();
            if (raw.contains("T")) {
                // aceita "yyyy-MM-dd'T'HH:mm" e "yyyy-MM-dd'T'HH:mm:ss"
                if (raw.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}$")) {
                    raw = raw + ":00"; // garante segundos
                }
                a.setDataEntrega(LocalDateTime.parse(raw)); // ISO
            } else {
                // veio só a data -> meia-noite
                a.setDataEntrega(LocalDate.parse(raw).atStartOfDay());
            }
        }

        // Disciplina (apenas id no request)
        Long discId = (req.disciplina() != null ? req.disciplina().id() : null);
        if (discId == null) throw new IllegalArgumentException("Disciplina é obrigatória");
        Disciplina disc = disciplinaRepository.findByIdAndUsuarioId(discId, usuarioId)
            .orElseThrow(() -> new EntityNotFoundException("Disciplina não encontrada: " + discId));
        a.setDisciplina(disc);

        // Integrantes (enviar/receber só ids)
        a.getIntegrantes().clear();
        if (a.getModalidade() == Modalidade.EM_GRUPO && req.integrantes() != null && !req.integrantes().isEmpty()) {
            List<Long> ids = req.integrantes().stream().map(ContatoRef::id).toList();
            if (!ids.isEmpty()) {
                if (ids.stream().anyMatch(java.util.Objects::isNull)) {
                    throw new IllegalArgumentException("Todos os contatos devem possuir um ID ao vincular a uma avaliação.");
                }
                List<Contato> contatos = contatoRepository.findByOwnerIdAndIdIn(usuarioId, ids);
                java.util.Set<Long> encontrados = new java.util.HashSet<>(contatos.stream().map(Contato::getId).toList());
                if (encontrados.size() != new java.util.HashSet<>(ids).size()) {
                    throw new EntityNotFoundException("Alguns contatos informados não pertencem ao usuário autenticado.");
                }
                a.getIntegrantes().addAll(contatos);
            }
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

    private Usuario referenciaUsuario(Long usuarioId) {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        return usuario;
    }
}
