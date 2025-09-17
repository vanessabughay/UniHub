package com.unihub.backend.service;

import com.unihub.backend.dto.anotacoes.AnotacoesRequest;
import com.unihub.backend.dto.anotacoes.AnotacoesResponse;
import com.unihub.backend.model.Anotacoes;
import com.unihub.backend.model.Disciplina;
import com.unihub.backend.repository.AnotacoesRepository;
import com.unihub.backend.repository.DisciplinaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.unihub.backend.exceptions.ResourceNotFoundException;

@Service
public class AnotacoesService {

    private final AnotacoesRepository anotacoesRepository;
    private final DisciplinaRepository disciplinaRepository;

    public AnotacoesService(AnotacoesRepository anotacoesRepository, DisciplinaRepository disciplinaRepository) {
        this.anotacoesRepository = anotacoesRepository;
        this.disciplinaRepository = disciplinaRepository;
    }

    @Transactional(readOnly = true)
    public Page<AnotacoesResponse> listar(Long disciplinaId, Pageable pageable) {
        validarDisciplina(disciplinaId);
        return anotacoesRepository.findAllByDisciplinaId(disciplinaId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AnotacoesResponse obter(Long disciplinaId, Long anotacoesId) {
        Anotacoes a = anotacoesRepository.findByIdAndDisciplinaId(anotacoesId, disciplinaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Anotação " + anotacoesId + " não encontrada na disciplina " + disciplinaId));
        return toResponse(a);
    }

    @Transactional
    public AnotacoesResponse criar(Long disciplinaId, AnotacoesRequest req) {
        Disciplina disc = validarDisciplina(disciplinaId);
        Anotacoes a = new Anotacoes();
        a.setTitulo(req.getTitulo().trim());
        a.setConteudo(req.getConteudo() == null ? "" : req.getConteudo().trim());
        a.setDisciplina(disc);
        a = anotacoesRepository.save(a);
        return toResponse(a);
    }

    @Transactional
    public AnotacoesResponse atualizar(Long disciplinaId, Long anotacoesId, AnotacoesRequest req) {
        Anotacoes a = anotacoesRepository.findByIdAndDisciplinaId(anotacoesId, disciplinaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Anotação " + anotacoesId + " não encontrada na disciplina " + disciplinaId));
        a.setTitulo(req.getTitulo().trim());
        a.setConteudo(req.getConteudo() == null ? "" : req.getConteudo().trim());
        // save não é obrigatório pois a entidade está gerenciada, mas não faz mal:
        a = anotacoesRepository.save(a);
        return toResponse(a);
    }

    @Transactional
    public void remover(Long disciplinaId, Long anotacoesId) {
        Anotacoes a = anotacoesRepository.findByIdAndDisciplinaId(anotacoesId, disciplinaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Anotação " + anotacoesId + " não encontrada na disciplina " + disciplinaId));
        anotacoesRepository.delete(a);
    }

    private Disciplina validarDisciplina(Long disciplinaId) {
        return disciplinaRepository.findById(disciplinaId)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina " + disciplinaId + " não encontrada"));
    }

    private AnotacoesResponse toResponse(Anotacoes a) {
        return new AnotacoesResponse(
                a.getId(),
                a.getTitulo(),
                a.getConteudo(),
                a.getDisciplina().getId(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }
}