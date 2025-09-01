package com.unihub.backend.service;

import com.unihub.backend.model.Contato;
import com.unihub.backend.repository.ContatoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContatoService {
    @Autowired
    private ContatoRepository repository;

    public List<Contato> listarTodas() {
        return repository.findAll();
    }

    public Contato salvar(Contato contato) {
        return repository.save(contato);
    }

    public Contato buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contato não encontrado"));
    }

    public void excluir(Long id) {
        repository.deleteById(id);
    }

    public List<Contato> buscarPorNome(String nome) {
        return repository.findByNomeContainingIgnoreCase(nome);
    }

}
