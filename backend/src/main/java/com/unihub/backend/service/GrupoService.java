package com.unihub.backend.service;

import com.unihub.backend.model.Contato;
import com.unihub.backend.model.Grupo;
import com.unihub.backend.repository.ContatoRepository;
import com.unihub.backend.repository.GrupoRepository;
import jakarta.persistence.EntityNotFoundException; // Boa prática para exceções específicas
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante para operações de modificação

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class GrupoService {

    @Autowired
    private GrupoRepository grupoRepository; // Renomeado para clareza

    @Autowired
    private ContatoRepository contatoRepository;

    @Transactional(readOnly = true)
    public List<Grupo> listarTodas(Long usuarioId) {
        Long ownerId = requireUsuario(usuarioId);
        return grupoRepository.findByOwnerId(ownerId);
    }

    @Transactional(readOnly = true)
        public Grupo buscarPorId(Long id, Long usuarioId) {
            Long ownerId = requireUsuario(usuarioId);
            return grupoRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Grupo não encontrado com ID: " + id));
    }

    @Transactional // Métodos que modificam dados devem ser transacionais
        public Grupo criarGrupo(Grupo grupo, Long usuarioId) {
            Long ownerId = requireUsuario(usuarioId);
            grupo.setId(null);
            grupo.setOwnerId(ownerId);
            List<Contato> membrosGerenciados = carregarMembrosValidos(grupo.getMembros(), ownerId);
            grupo.getMembros().clear();
            grupo.getMembros().addAll(membrosGerenciados);

        return grupoRepository.save(grupo);
    }

    @Transactional
        public Grupo atualizarGrupo(Long id, Grupo grupoDetalhesRequest, Long usuarioId) {
            Long ownerId = requireUsuario(usuarioId);
            Grupo grupoExistente = buscarPorId(id, ownerId);

        if (grupoDetalhesRequest.getNome() != null) {
            grupoExistente.setNome(grupoDetalhesRequest.getNome());
        }

        // Lógica para atualizar a lista de membros
        if (grupoDetalhesRequest.getMembros() != null) {
            List<Contato> novosMembros = carregarMembrosValidos(grupoDetalhesRequest.getMembros(), ownerId);
            grupoExistente.getMembros().clear();
            grupoExistente.getMembros().addAll(novosMembros);
        }
        return grupoRepository.save(grupoExistente);
    }

    @Transactional
        public void excluir(Long id, Long usuarioId) {
            Long ownerId = requireUsuario(usuarioId);
            if (!grupoRepository.existsByIdAndOwnerId(id, ownerId)) {
            throw new EntityNotFoundException("Grupo não encontrado com ID: " + id + " para exclusão.");
        }
        grupoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
        public List<Grupo> buscarPorNome(String nome, Long usuarioId) {
            Long ownerId = requireUsuario(usuarioId);
            return grupoRepository.findByNomeContainingIgnoreCaseAndOwnerId(nome, ownerId);
    }

        private List<Contato> carregarMembrosValidos(List<Contato> membros, Long ownerId) {
            if (membros == null || membros.isEmpty()) {
                return new ArrayList<>();
            }

            List<Long> ids = membros.stream()
                    .map(Contato::getId)
                    .toList();

            if (ids.stream().anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException("Todos os contatos devem possuir um ID ao vincular a um grupo.");
            }

            List<Contato> encontrados = contatoRepository.findByOwnerIdAndIdIn(ownerId, ids);
            Set<Long> encontradosIds = new HashSet<>(encontrados.stream().map(Contato::getId).toList());
            if (encontradosIds.size() != new HashSet<>(ids).size()) {
                throw new EntityNotFoundException("Alguns contatos informados não pertencem ao usuário autenticado.");
            }

            return new ArrayList<>(encontrados);
        }

        private Long requireUsuario(Long usuarioId) {
            if (usuarioId == null) {
                throw new IllegalArgumentException("Usuário autenticado é obrigatório");
            }
            return usuarioId;
        }
}
