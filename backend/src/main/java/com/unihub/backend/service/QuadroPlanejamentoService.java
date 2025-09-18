package com.unihub.backend.service;

import com.unihub.backend.model.QuadroPlanejamento;
import com.unihub.backend.model.Usuario;
import com.unihub.backend.model.enums.QuadroStatus;
import com.unihub.backend.repository.QuadroPlanejamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuadroPlanejamentoService {

    @Autowired
    private QuadroPlanejamentoRepository repository;

    public List<QuadroPlanejamento> listar(Long usuarioId, QuadroStatus status, String titulo) {
        boolean possuiTitulo = titulo != null && !titulo.trim().isEmpty();

        if (status != null && possuiTitulo) {
            return repository.findByUsuarioIdAndStatusAndTituloContainingIgnoreCase(usuarioId, status, titulo);
        }

        if (status != null) {
            return repository.findByUsuarioIdAndStatus(usuarioId, status);
        }

        if (possuiTitulo) {
            return repository.findByUsuarioIdAndTituloContainingIgnoreCase(usuarioId, titulo);
        }

        return repository.findByUsuarioId(usuarioId);
    }

    public QuadroPlanejamento buscarPorId(Long id, Long usuarioId) {
        return repository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("Quadro de Planejamento não encontrado"));
    }

    public QuadroPlanejamento criar(QuadroPlanejamento quadro, Long usuarioId) {
        quadro.setId(null);
        quadro.setDataCriacao(LocalDateTime.now());
        quadro.setUsuario(referenciaUsuario(usuarioId));
        ajustarStatus(quadro);
        return repository.save(quadro);
    }

    public QuadroPlanejamento atualizar(Long id, QuadroPlanejamento quadroAtualizado, Long usuarioId) {
        QuadroPlanejamento existente = buscarPorId(id, usuarioId);

        existente.setTitulo(quadroAtualizado.getTitulo());
        existente.setDescricao(quadroAtualizado.getDescricao());

        if (quadroAtualizado.getStatus() != null) {
            existente.setStatus(quadroAtualizado.getStatus());
        }

        if (quadroAtualizado.getDataEncerramento() != null) {
            existente.setDataEncerramento(quadroAtualizado.getDataEncerramento());
        }

        ajustarStatus(existente);

        return repository.save(existente);
    }

    private Usuario referenciaUsuario(Long usuarioId) {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        return usuario;
    }

    private void ajustarStatus(QuadroPlanejamento quadro) {
        if (quadro.getStatus() == null) {
            quadro.setStatus(QuadroStatus.ATIVO);
        }

        if (quadro.getStatus() == QuadroStatus.ENCERRADO) {
            if (quadro.getDataEncerramento() == null) {
                quadro.setDataEncerramento(LocalDateTime.now());
            }
        } else {
            quadro.setDataEncerramento(null);
        }
    }
}