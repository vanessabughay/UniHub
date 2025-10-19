package com.unihub.backend.service;

import com.unihub.backend.model.Contato;
import com.unihub.backend.model.Grupo;
import com.unihub.backend.repository.ContatoRepository;
import com.unihub.backend.repository.GrupoRepository;
import jakarta.persistence.EntityNotFoundException; // Boa prática para exceções específicas
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante para operações de modificação
import com.unihub.backend.model.Usuario;
import com.unihub.backend.repository.UsuarioRepository;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class GrupoService {

    @Autowired
    private GrupoRepository grupoRepository; // Renomeado para clareza

    @Autowired
    private ContatoRepository contatoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<Grupo> listarTodas(Long usuarioId) {
        Long ownerId = requireUsuario(usuarioId);
        List<Grupo> grupos = grupoRepository.findByOwnerId(ownerId);
        grupos.forEach(grupo -> definirAdminContato(grupo, null));
        return grupos;
    }

    @Transactional(readOnly = true)
    public Grupo buscarPorId(Long id, Long usuarioId) {
        Long ownerId = requireUsuario(usuarioId);
        Grupo grupo = grupoRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Grupo não encontrado com ID: " + id));
        definirAdminContato(grupo, null);
        return grupo;
    }

    @Transactional // Métodos que modificam dados devem ser transacionais
    public Grupo criarGrupo(Grupo grupo, Long usuarioId) {
        Long ownerId = requireUsuario(usuarioId);
        grupo.setId(null);
        grupo.setOwnerId(ownerId);
        List<Contato> membrosGerenciados = carregarMembrosValidos(grupo.getMembros(), ownerId);
        Contato contatoAdministrador = garantirContatoDoUsuarioNoGrupo(membrosGerenciados, ownerId, true);
        grupo.getMembros().clear();
        grupo.getMembros().addAll(membrosGerenciados);
        definirAdminContato(grupo, contatoAdministrador);
        Grupo grupoSalvo = grupoRepository.save(grupo);
        definirAdminContato(grupoSalvo, contatoAdministrador);
        return grupoSalvo;
    }

    @Transactional
    public Grupo atualizarGrupo(Long id, Grupo grupoDetalhesRequest, Long usuarioId) {
        Long ownerId = requireUsuario(usuarioId);
        Grupo grupoExistente = buscarPorId(id, ownerId);

        if (grupoDetalhesRequest.getNome() != null) {
            grupoExistente.setNome(grupoDetalhesRequest.getNome());
        }

        // Lógica para atualizar a lista de membros
        Contato contatoAdministradorPreferencial;
        if (grupoDetalhesRequest.getMembros() != null) {
            List<Contato> novosMembros = carregarMembrosValidos(grupoDetalhesRequest.getMembros(), ownerId);
            contatoAdministradorPreferencial = garantirContatoDoUsuarioNoGrupo(novosMembros, ownerId, false);
            grupoExistente.getMembros().clear();
            grupoExistente.getMembros().addAll(novosMembros);
        } else {
            contatoAdministradorPreferencial = garantirContatoDoUsuarioNoGrupo(grupoExistente.getMembros(), ownerId, false);
        }
        definirAdminContato(grupoExistente, contatoAdministradorPreferencial);
        Grupo grupoAtualizado = grupoRepository.save(grupoExistente);
        definirAdminContato(grupoAtualizado, contatoAdministradorPreferencial);
        return grupoAtualizado;
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
        List<Grupo> grupos = grupoRepository.findByNomeContainingIgnoreCaseAndOwnerId(nome, ownerId);
        grupos.forEach(grupo -> definirAdminContato(grupo, null));
        return grupos;
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

    private Contato garantirContatoDoUsuarioNoGrupo(List<Contato> membros, Long ownerId, boolean forcarInclusao) {
        if (membros == null) {
            return null;
        }

        Contato contatoDoUsuario = obterOuCriarContatoDoUsuario(ownerId);

        if (contatoDoUsuario == null) {
            return null;
        }

        boolean jaPresente = membros.stream()
                .filter(contato -> contato != null && contato.getId() != null)
                .anyMatch(contato -> contato.getId().equals(contatoDoUsuario.getId()));
        if (!jaPresente && (forcarInclusao || membros.isEmpty())) {
            membros.add(contatoDoUsuario);
            jaPresente = true;
        }

        if (jaPresente && contatoDoUsuario.getId() != null) {
            Long contatoId = contatoDoUsuario.getId();
            return membros.stream()
                    .filter(contato -> contato != null && contatoId.equals(contato.getId()))
                    .findFirst()
                    .orElse(contatoDoUsuario);
        }

        return jaPresente ? contatoDoUsuario : null;
    }

    private void definirAdminContato(Grupo grupo, Contato contatoPreferencial) {
        if (grupo == null) {
            return;
        }

        List<Contato> membros = grupo.getMembros();
        if (membros == null || membros.isEmpty()) {
            grupo.setAdminContatoId(null);
            return;
        }

        Long candidato = null;

        if (contatoPreferencial != null && contatoPreferencial.getId() != null) {
            candidato = contatoPreferencial.getId();
        }

        if (candidato == null && grupo.getOwnerId() != null) {
            candidato = membros.stream()
                    .filter(Objects::nonNull)
                    .filter(contato -> Objects.equals(contato.getOwnerId(), grupo.getOwnerId()))
                    .map(Contato::getId)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        if (candidato == null) {
            candidato = grupo.getAdminContatoId();
        }

        if (candidato != null) {
            Long finalCandidato = candidato;
            boolean aindaNoGrupo = membros.stream()
                    .filter(Objects::nonNull)
                    .anyMatch(contato -> contato.getId() != null && contato.getId().equals(finalCandidato));
            if (aindaNoGrupo) {
                grupo.setAdminContatoId(candidato);
                return;
            }
        }

        Long novoAdmin = membros.stream()
                .filter(Objects::nonNull)
                .map(Contato::getId)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);

        grupo.setAdminContatoId(novoAdmin);
    }

    private Contato obterOuCriarContatoDoUsuario(Long ownerId) {
        Usuario usuario = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + ownerId));

        String email = usuario.getEmail();
        Optional<Contato> existente = Optional.empty();
        if (email != null && !email.isBlank()) {
            existente = contatoRepository.findByOwnerIdAndEmail(ownerId, email);
        }

        if (existente.isPresent()) {
            return existente.get();
        }
        Contato contato = new Contato();
        contato.setOwnerId(ownerId);
        contato.setNome(usuario.getNomeUsuario() != null ? usuario.getNomeUsuario() : "Usuário " + ownerId);
        contato.setEmail(email);
        contato.setPendente(Boolean.FALSE);
        return contatoRepository.save(contato);
    }

    private Long requireUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("Usuário autenticado é obrigatório");
        }
        return usuarioId;
    }
}