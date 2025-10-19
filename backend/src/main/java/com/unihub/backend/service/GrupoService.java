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
        Contato contatoDoUsuario = buscarContatoDoUsuario(ownerId).orElse(null);
        grupos.forEach(grupo -> definirAdminContato(grupo, contatoDoUsuario, contatoDoUsuario));
        return grupos;
    }

    @Transactional(readOnly = true)
    public Grupo buscarPorId(Long id, Long usuarioId) {
        Long ownerId = requireUsuario(usuarioId);
        Grupo grupo = grupoRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Grupo não encontrado com ID: " + id));
        Contato contatoDoUsuario = buscarContatoDoUsuario(ownerId).orElse(null);
        definirAdminContato(grupo, contatoDoUsuario, contatoDoUsuario);
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
        definirAdminContato(grupo, contatoAdministrador, contatoAdministrador);
        Grupo grupoSalvo = grupoRepository.save(grupo);
        definirAdminContato(grupoSalvo, contatoAdministrador, contatoAdministrador);
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
        Contato contatoDoUsuario = contatoAdministradorPreferencial != null
                ? contatoAdministradorPreferencial
                : buscarContatoDoUsuario(ownerId).orElse(null);
        definirAdminContato(grupoExistente, contatoAdministradorPreferencial, contatoDoUsuario);
        Grupo grupoAtualizado = grupoRepository.save(grupoExistente);
        definirAdminContato(grupoAtualizado, contatoAdministradorPreferencial, contatoDoUsuario);
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
        Contato contatoDoUsuario = buscarContatoDoUsuario(ownerId).orElse(null);
        grupos.forEach(grupo -> definirAdminContato(grupo, contatoDoUsuario, contatoDoUsuario));
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

    private void definirAdminContato(Grupo grupo, Contato contatoPreferencial, Contato contatoDoUsuario) {
        if (grupo == null) {
            return;
        }

        List<Contato> membros = grupo.getMembros();
        if (membros == null || membros.isEmpty()) {
            grupo.setAdminContatoId(null);
            return;
        }

        Long candidato = null;

        if (contatoPreferencial != null && contatoPreferencial.getId() != null
                && isContatoNoGrupo(membros, contatoPreferencial.getId())) {
            candidato = contatoPreferencial.getId();
        } else if (contatoDoUsuario != null && contatoDoUsuario.getId() != null
                && isContatoNoGrupo(membros, contatoDoUsuario.getId())) {
            candidato = contatoDoUsuario.getId();
        } else if (grupo.getAdminContatoId() != null && isContatoNoGrupo(membros, grupo.getAdminContatoId())) {
            candidato = grupo.getAdminContatoId();
        }

        if (candidato == null) {
            candidato = membros.stream()
                    .filter(Objects::nonNull)

                    .map(Contato::getId)
                    .filter(Objects::nonNull)
                    .min(Comparator.naturalOrder())
                    .orElse(null);
        }

        grupo.setAdminContatoId(candidato);
    }

    private boolean isContatoNoGrupo(List<Contato> membros, Long contatoId) {
        if (contatoId == null) {
            return false;
        }
        return membros.stream()
                .filter(Objects::nonNull)
                .anyMatch(contato -> contatoId.equals(contato.getId()));
    }
    private Optional<Contato> buscarContatoDoUsuario(Long ownerId) {
        if (ownerId == null) {
            return Optional.empty();
        }

        Usuario usuario = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + ownerId));

        String email = usuario.getEmail();
        if (email != null && !email.isBlank()) {
            Optional<Contato> contatoPorEmail = contatoRepository.findByOwnerIdAndEmailIgnoreCase(ownerId, email);
            if (contatoPorEmail.isPresent()) {
                return contatoPorEmail;

            }
        }

        return contatoRepository.findByOwnerId(ownerId).stream()
                .filter(Objects::nonNull)
                .filter(contato -> !Boolean.TRUE.equals(contato.getPendente()))
                .sorted(Comparator.comparing(Contato::getId))
                .findFirst();
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