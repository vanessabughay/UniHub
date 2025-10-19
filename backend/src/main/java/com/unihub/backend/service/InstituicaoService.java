package com.unihub.backend.service;

import com.unihub.backend.model.Instituicao;
import com.unihub.backend.model.Usuario;
import com.unihub.backend.repository.InstituicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstituicaoService {

    @Autowired
    private InstituicaoRepository repository;

    public List<Instituicao> buscarPorNome(String nome, Long usuarioId) {
        if (nome == null || nome.isBlank()) {
            if (usuarioId == null) {
                return List.of();
            }
            return repository.findByUsuarioIdOrderByNomeAsc(usuarioId);
                }

        return repository.findByNomeContainingIgnoreCaseOrderByNomeAsc(nome.trim());
    }


    public Instituicao salvar(Instituicao instituicao, Long usuarioId) {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        instituicao.setUsuario(usuario);
        return repository.save(instituicao);
    }

    public Instituicao atualizar(Long id, Instituicao instituicao, Long usuarioId) {
        Instituicao existente = repository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("Instituição não encontrada"));
        existente.setNome(instituicao.getNome());
        existente.setMediaAprovacao(instituicao.getMediaAprovacao());
        existente.setFrequenciaMinima(instituicao.getFrequenciaMinima());
        return repository.save(existente);
    }
}