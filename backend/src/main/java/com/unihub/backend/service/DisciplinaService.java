package com.unihub.backend.service;

import com.unihub.backend.model.Disciplina;
import com.unihub.backend.repository.DisciplinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service 
public class DisciplinaService {

    @Autowired
    private DisciplinaRepository repository;

    public List<Disciplina> listarTodas() {
        return repository.findAll();
    }

    public Disciplina salvar(Disciplina disciplina) {
        return repository.save(disciplina);
    }

     public Disciplina buscarPorId(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Disciplina não encontrada"));
    }

    public void excluir(Long id) {
        repository.deleteById(id);
    }

    public List<Disciplina> buscarPorNome(String nome) {
    return repository.findByNomeContainingIgnoreCase(nome);
    }

}
