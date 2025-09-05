package com.unihub.backend.service;

import com.unihub.backend.model.Grupo;
import com.unihub.backend.repository.GrupoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GrupoService {
    @Autowired
    private GrupoRepository repository;

    public List<Grupo> listarTodas() {
        return repository.findAll();
    }

    public Grupo salvar(Grupo grupo) {
        return repository.save(grupo);
    }

    public Grupo buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado"));
    }

    public void excluir(Long id) {
        repository.deleteById(id);
    }

    public List<Grupo> buscarPorNome(String nome) {
        return repository.findByNomeContainingIgnoreCase(nome);
    }
}