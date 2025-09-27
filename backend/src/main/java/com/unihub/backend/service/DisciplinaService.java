package com.unihub.backend.service;

import com.unihub.backend.model.Disciplina;
import com.unihub.backend.model.Usuario;
import com.unihub.backend.repository.AvaliacaoRepository;
import com.unihub.backend.repository.DisciplinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service 
public class DisciplinaService {

    @Autowired
    private DisciplinaRepository repository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    public List<Disciplina> listarTodas(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    public Disciplina salvar(Disciplina disciplina, Long usuarioId) {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        disciplina.setUsuario(usuario);
        return repository.save(disciplina);
    }

    public Disciplina buscarPorId(Long id, Long usuarioId) {
        return repository.findByIdAndUsuarioId(id, usuarioId)
            .orElseThrow(() -> new RuntimeException("Disciplina não encontrada"));
    }

    public void excluir(Long id, Long usuarioId) {
        Disciplina disciplina = buscarPorId(id, usuarioId);
        repository.delete(disciplina);
    }

    public List<Disciplina> buscarPorNome(String nome, Long usuarioId) {
        return repository.findByUsuarioIdAndNomeContainingIgnoreCase(usuarioId, nome);
    }

}
