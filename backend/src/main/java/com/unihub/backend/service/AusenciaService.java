package com.unihub.backend.service;

import com.unihub.backend.model.Ausencia;
import com.unihub.backend.model.Usuario;
import com.unihub.backend.repository.AusenciaRepository;
import com.unihub.backend.model.Disciplina;
import com.unihub.backend.repository.DisciplinaRepository;
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


    public List<Ausencia> listarTodas(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    public Ausencia salvar(Ausencia ausencia, Long usuarioId) {
        if (ausencia.getCategoria() != null && !ausencia.getCategoria().isBlank()) {
            categoriaService.buscarOuCriar(ausencia.getCategoria(), usuarioId);
        }
        if (ausencia.getDisciplinaId() != null) {
            Disciplina disciplina = disciplinaRepository.findByIdAndUsuarioId(ausencia.getDisciplinaId(), usuarioId)
                    .orElseThrow(() -> new RuntimeException("Disciplina com ID " + ausencia.getDisciplinaId() + " não encontrada para este usuário."));
            ausencia.setDisciplina(disciplina);
        }
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        ausencia.setUsuario(usuario);
        return repository.save(ausencia);
    }

    public Ausencia buscarPorId(Long id, Long usuarioId) {
        return repository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("Ausência não encontrada"));
    }

    public void excluir(Long id, Long usuarioId) {
        Ausencia ausencia = buscarPorId(id, usuarioId);
        repository.delete(ausencia);
    }
}