package com.unihub.backend.service;

import com.unihub.backend.model.Ausencia;
import com.unihub.backend.repository.AusenciaRepository;
import com.unihub.backend.repository.DisciplinaRepository;
import com.unihub.backend.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AusenciaService {

    @Autowired
    private AusenciaRepository repository;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private DisciplinaRepository disciplinaRepository;


    public List<Ausencia> listarTodas() {
        return repository.findAll();
    }

    public Ausencia salvar(Ausencia ausencia) {
        if (ausencia.getCategoria() != null && !ausencia.getCategoria().isBlank()) {
            categoriaService.buscarOuCriar(ausencia.getCategoria());
        }
        if (ausencia.getDisciplinaId() != null) {
            disciplinaRepository.findById(ausencia.getDisciplinaId())
                    .ifPresent(ausencia::setDisciplina);
        }
        return repository.save(ausencia);
    }

    public Ausencia buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ausência não encontrada"));
    }

    public void excluir(Long id) {
        repository.deleteById(id);
    }
}