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

    public List<Instituicao> listarTodas(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    public List<Instituicao> buscarPorNome(String nome, Long usuarioId) {
        return repository.findByUsuarioIdAndNomeContainingIgnoreCase(usuarioId, nome);
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