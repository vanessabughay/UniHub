package com.unihub.backend.service;

import com.unihub.backend.model.Categoria;
import com.unihub.backend.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository repository;

    public List<Categoria> listarTodas() {
        return repository.findAll();
    }

    public Categoria salvar(Categoria categoria) {
        return repository.save(categoria);
    }

    public Categoria buscarOuCriar(String nome) {
        return repository.findByNome(nome).orElseGet(() -> {
            Categoria nova = new Categoria();
            nova.setNome(nome);
            return repository.save(nova);
        });
    }
}