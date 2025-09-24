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
import java.util.List;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final ContatoRepository contatoRepository;
    private final DisciplinaRepository disciplinaRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository,
                            ContatoRepository contatoRepository,
                            DisciplinaRepository disciplinaRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.contatoRepository = contatoRepository;
        this.disciplinaRepository = disciplinaRepository;
    }

    @Transactional(readOnly = true)
    public List<Avaliacao> listarTodas() {
        return avaliacaoRepository.findAllBy();
    }

    @Transactional(readOnly = true)
    public Avaliacao buscarPorId(Long id) {
        return avaliacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Avaliação não encontrada: " + id));
    }

    @Transactional
    public Long criar(AvaliacaoRequest req, Long usuarioId) {
        Avaliacao a = new Avaliacao();
        aplicar(req, a, usuarioId);
        return avaliacaoRepository.save(a).getId();
    }

    @Transactional
    public boolean atualizar(Long id, AvaliacaoRequest req, Long usuarioId) {
        var opt = avaliacaoRepository.findById(id);
        if (opt.isEmpty()) return false;
        Avaliacao a = opt.get();
        aplicar(req, a, usuarioId);
        avaliacaoRepository.save(a);
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

        if (req.dataEntrega() == null || req.dataEntrega().isBlank()) {
            a.setDataEntrega(null);
        } else {
            a.setDataEntrega(LocalDate.parse(req.dataEntrega())); // "AAAA-MM-DD"
        }

        // Disciplina (apenas id no request)
        Long discId = (req.disciplina() != null ? req.disciplina().id() : null);
        if (discId == null) throw new IllegalArgumentException("Disciplina é obrigatória");
        Disciplina disc = disciplinaRepository.findById(discId)
                .orElseThrow(() -> new EntityNotFoundException("Disciplina não encontrada: " + discId));
        a.setDisciplina(disc);

        // Integrantes (enviar/receber só ids)
        a.getIntegrantes().clear();
        if (a.getModalidade() == Modalidade.EM_GRUPO && req.integrantes() != null && !req.integrantes().isEmpty()) {
            List<Long> ids = req.integrantes().stream().map(ContatoRef::id).toList();
            if (!ids.isEmpty()) {
                List<Contato> contatos = contatoRepository.findAllById(ids);
                a.getIntegrantes().addAll(contatos);
            }
        }
    }

    @Transactional
    public void excluir(Long id) {
        Avaliacao a = buscarPorId(id);
        avaliacaoRepository.delete(a);
    }

    @Transactional(readOnly = true)
    public List<Avaliacao> buscarPorNome(String descricao) {
        return avaliacaoRepository.findByDescricaoContainingIgnoreCaseAndDisciplinaIsNotNull(descricao);
    }
}
