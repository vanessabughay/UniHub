package com.unihub.backend.service;

import com.unihub.backend.model.Instituicao;
import com.unihub.backend.repository.InstituicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstituicaoService {

    @Autowired
    private InstituicaoRepository repository;

    public List<Instituicao> listarTodas() {
        return repository.findAll();
    }

    public List<Instituicao> buscarPorNome(String nome) {
        return repository.findByNomeContainingIgnoreCase(nome);
    }

    public Instituicao salvar(Instituicao instituicao) {
        return repository.save(instituicao);
    }

    public Instituicao atualizar(Long id, Instituicao instituicao) {
        instituicao.setId(id);
        return repository.save(instituicao);
    }
}