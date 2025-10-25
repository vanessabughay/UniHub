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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        Contato contatoProprio = buscarContatoDoUsuario(ownerId).orElse(null);

        Map<Long, Grupo> gruposVisiveis = new LinkedHashMap<>();
        grupoRepository.findByOwnerId(ownerId)
                .forEach(grupo -> gruposVisiveis.put(grupo.getId(), grupo));

        Set<Long> idsContatosAssociados = buscarIdsDeContatosDoUsuario(ownerId);
        if (!idsContatosAssociados.isEmpty()) {
            grupoRepository.findDistinctByMembros_IdContatoIn(idsContatosAssociados)
                    .forEach(grupo -> gruposVisiveis.put(grupo.getId(), grupo));
        }

        gruposVisiveis.values().forEach(grupo -> {
                Contato preferenciaAdmin = Objects.equals(grupo.getOwnerId(), ownerId)
                    ? contatoProprio
                    : localizarContatoDoOwner(grupo);
                                definirAdminContato(grupo, preferenciaAdmin, preferenciaAdmin, false);
        });
        return new ArrayList<>(gruposVisiveis.values());
    }

    @Transactional(readOnly = true)
    public Grupo buscarPorId(Long id, Long usuarioId) {
        Long ownerId = requireUsuario(usuarioId);
        Optional<Grupo> grupoOptional = grupoRepository.findByIdAndOwnerId(id, ownerId);

        if (grupoOptional.isEmpty()) {
            Set<Long> idsContatosAssociados = buscarIdsDeContatosDoUsuario(ownerId);
            if (!idsContatosAssociados.isEmpty()) {
                grupoOptional = grupoRepository.findDistinctByIdAndMembros_IdContatoIn(id, idsContatosAssociados);
            }
        }

        Grupo grupo = grupoOptional
                .orElseThrow(() -> new EntityNotFoundException("Grupo não encontrado com ID: " + id));
        Contato preferenciaAdmin = Objects.equals(grupo.getOwnerId(), ownerId)
                ? buscarContatoDoUsuario(ownerId).orElse(null)
                : localizarContatoDoOwner(grupo);
                definirAdminContato(grupo, preferenciaAdmin, preferenciaAdmin, false);
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
        Contato administrador = definirAdminContato(grupo, contatoAdministrador, contatoAdministrador, false);
        boolean ownerAlterado = atualizarOwnerPorAdmin(grupo, administrador);
        Grupo grupoSalvo = grupoRepository.save(grupo);
        Contato administradorSalvo = definirAdminContato(grupoSalvo, contatoAdministrador, contatoAdministrador, false);
        if (atualizarOwnerPorAdmin(grupoSalvo, administradorSalvo) && !ownerAlterado) {
            grupoSalvo = grupoRepository.save(grupoSalvo);
        }
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
        if (grupoExistente.getMembros().isEmpty()) {
            grupoRepository.delete(grupoExistente);
            throw new EntityNotFoundException("Grupo removido por não possuir membros ativos.");
        }

        Contato contatoDoUsuario = contatoAdministradorPreferencial != null
                ? contatoAdministradorPreferencial
                : buscarContatoDoUsuario(ownerId).orElse(null);

        Contato administrador = definirAdminContato(grupoExistente, contatoAdministradorPreferencial, contatoDoUsuario, true);
        boolean ownerAlterado = atualizarOwnerPorAdmin(grupoExistente, administrador);
        Grupo grupoAtualizado = grupoRepository.save(grupoExistente);
        Contato administradorAtualizado = definirAdminContato(grupoAtualizado, contatoAdministradorPreferencial, contatoDoUsuario, true);
        if (atualizarOwnerPorAdmin(grupoAtualizado, administradorAtualizado) && !ownerAlterado) {
            grupoAtualizado = grupoRepository.save(grupoAtualizado);
        }
        return grupoAtualizado;
    }

    @Transactional
    public void excluir(Long id, Long usuarioId) {
        Long ownerId = requireUsuario(usuarioId);
        Grupo grupo = grupoRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Grupo não encontrado com ID: " + id + " para exclusão."));

        Contato contatoDoOwner = localizarContatoDoOwner(grupo);
        if (contatoDoOwner != null) {
            grupo.removeMembro(contatoDoOwner);
        }

        List<Contato> membros = grupo.getMembros();
        if (membros == null || membros.isEmpty()) {
            grupoRepository.delete(grupo);
            return;
        }

        membros.removeIf(Objects::isNull);

        if (membros.isEmpty()) {
            grupoRepository.delete(grupo);
            return;
        }

        Contato novoAdministrador = selecionarNovoOwner(grupo);
        if (novoAdministrador == null) {
            throw new IllegalStateException("Não foi possível transferir a propriedade do grupo para outro membro.");
        }

        if (!atualizarOwnerPorAdmin(grupo, novoAdministrador)) {
            throw new IllegalStateException("Não foi possível definir um novo proprietário para o grupo.");
        }
        definirAdminContato(grupo, novoAdministrador, novoAdministrador, false);
        grupoRepository.save(grupo);
    }

    @Transactional
    public void sairDoGrupo(Long id, Long usuarioId) {
        Long usuarioAutenticado = requireUsuario(usuarioId);
        Grupo grupo = buscarGrupoParticipante(id, usuarioAutenticado);

        Usuario usuario = usuarioRepository.findById(usuarioAutenticado)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + usuarioAutenticado));

        boolean ehOwner = Objects.equals(grupo.getOwnerId(), usuarioAutenticado);
        if (grupo.getMembros() == null) {
            grupo.setMembros(new ArrayList<>());
        }

        boolean removeu = removerUsuarioDosMembros(grupo, usuario);
        if (ehOwner && !removeu) {
            Contato contatoDoOwner = localizarContatoDoOwner(grupo);
            if (contatoDoOwner != null) {
                grupo.removeMembro(contatoDoOwner);
                removeu = true;
            }
        }

        if (!removeu && !ehOwner) {
            throw new EntityNotFoundException("Usuário não pertence ao grupo informado.");
        }


        if (removeu) {
            Long usuarioRemovidoId = usuario.getId();
            grupo.getMembros().removeIf(contato -> contato != null
                    && usuarioRemovidoId != null
                    && Objects.equals(usuarioRemovidoId, contato.getIdContato()));
        }

        List<Contato> membros = grupo.getMembros();
        if (membros == null || membros.isEmpty()) {
            if (ehOwner) {
                grupoRepository.delete(grupo);
                return;
            }
            grupoRepository.save(grupo);
            return;
        }

        if (ehOwner) {
            Contato novoOwner = selecionarNovoOwner(grupo);
            if (novoOwner == null) {
                grupoRepository.delete(grupo);
                return;
            }
            if (!atualizarOwnerPorAdmin(grupo, novoOwner)) {
                throw new IllegalStateException("Não foi possível transferir a propriedade do grupo para outro membro.");
            }
            definirAdminContato(grupo, novoOwner, novoOwner, false);
        } else {
            Contato contatoPreferencial = localizarContatoDoOwner(grupo);
            Contato contatoDoOwner = contatoPreferencial != null
                    ? contatoPreferencial
                    : buscarContatoDoUsuario(grupo.getOwnerId()).orElse(null);
            definirAdminContato(grupo, contatoPreferencial, contatoDoOwner, false);
        }

        grupoRepository.save(grupo);
    }


    @Transactional(readOnly = true)
    public List<Grupo> buscarPorNome(String nome, Long usuarioId) {
        Long ownerId = requireUsuario(usuarioId);
        Contato contatoProprio = buscarContatoDoUsuario(ownerId).orElse(null);

        Map<Long, Grupo> gruposVisiveis = new LinkedHashMap<>();
        grupoRepository.findByNomeContainingIgnoreCaseAndOwnerId(nome, ownerId)
                .forEach(grupo -> gruposVisiveis.put(grupo.getId(), grupo));

        Set<Long> idsContatosAssociados = buscarIdsDeContatosDoUsuario(ownerId);
        if (!idsContatosAssociados.isEmpty()) {
            grupoRepository.findDistinctByNomeContainingIgnoreCaseAndMembros_IdContatoIn(nome, idsContatosAssociados)
                    .forEach(grupo -> gruposVisiveis.put(grupo.getId(), grupo));
        }

        gruposVisiveis.values().forEach(grupo -> {
            Contato preferenciaAdmin = Objects.equals(grupo.getOwnerId(), ownerId)
                    ? contatoProprio
                    : localizarContatoDoOwner(grupo);
                definirAdminContato(grupo, preferenciaAdmin, preferenciaAdmin, false);
        });

        return new ArrayList<>(gruposVisiveis.values());
    }

    private Grupo buscarGrupoParticipante(Long grupoId, Long usuarioId) {
        Optional<Grupo> grupoOptional = grupoRepository.findByIdAndOwnerId(grupoId, usuarioId);

        if (grupoOptional.isEmpty()) {
            Set<Long> idsContatosAssociados = buscarIdsDeContatosDoUsuario(usuarioId);
            if (!idsContatosAssociados.isEmpty()) {
                grupoOptional = grupoRepository.findDistinctByIdAndMembros_IdContatoIn(grupoId, idsContatosAssociados);
            }
        }

        return grupoOptional
                .orElseThrow(() -> new EntityNotFoundException("Grupo não encontrado com ID: " + grupoId));
    }

    private boolean removerUsuarioDosMembros(Grupo grupo, Usuario usuario) {
        if (grupo == null || usuario == null) {
            return false;
        }

        List<Contato> membros = grupo.getMembros();
        if (membros == null || membros.isEmpty()) {
            return false;
        }

        boolean removeu = false;
        Iterator<Contato> iterator = membros.iterator();
        Long usuarioId = usuario.getId();
        while (iterator.hasNext()) {
            Contato membro = iterator.next();
            if (representaUsuario(membro, usuario)
                    || (usuarioId != null && membro != null && usuarioId.equals(membro.getIdContato()))) {
                iterator.remove();
                removeu = true;
            }
        }
        return removeu;
    }

    private boolean representaUsuario(Contato contato, Usuario usuario) {
        if (contato == null || usuario == null) {
            return false;
        }

        Long usuarioId = usuario.getId();
        if (usuarioId != null && Objects.equals(contato.getIdContato(), usuarioId)) {
            return true;
        }

        String emailUsuario = usuario.getEmail();
        if (emailUsuario != null && !emailUsuario.isBlank()) {
            String emailContato = contato.getEmail();
            if (emailContato != null && emailUsuario.equalsIgnoreCase(emailContato)) {
                return true;
            }
        }

        return false;
    }


    private Set<Long> buscarIdsDeContatosDoUsuario(Long usuarioId) {
        Set<Long> ids = new HashSet<>();
        if (usuarioId == null) {
            return ids;
        }

        ids.add(usuarioId);

        contatoRepository.findByIdContato(usuarioId).stream()
                .filter(Objects::nonNull)
                .map(Contato::getIdContato)
                .filter(Objects::nonNull)
                .forEach(ids::add);


        usuarioRepository.findById(usuarioId).ifPresent(usuario -> {
            String email = usuario.getEmail();
            if (email != null && !email.isBlank()) {
                contatoRepository.findByEmailIgnoreCase(email).stream()
                        .filter(Objects::nonNull)
                        .map(Contato::getIdContato)
                        .filter(Objects::nonNull)
                        .forEach(ids::add);
            }
        });
        return ids;
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

    private Contato definirAdminContato(Grupo grupo, Contato contatoPreferencial, Contato contatoDoUsuario, boolean permitirTransferencia) {
        if (grupo == null) {
            return null;
        }

        List<Contato> membros = grupo.getMembros();
        if (membros == null || membros.isEmpty()) {
            grupo.setAdminContatoId(null);
            return null;
        }

        Long ownerId = grupo.getOwnerId();
        if (ownerId == null) {
            grupo.setAdminContatoId(null);
            return null;
        }

        String ownerEmail = usuarioRepository.findById(ownerId)
                .map(Usuario::getEmail)
                .orElse(null);

        Contato candidato = null;

        if (contatoPreferencial != null && contatoPreferencial.getId() != null
                && isContatoNoGrupo(membros, contatoPreferencial.getId())) {
            candidato = localizarContatoPorId(membros, contatoPreferencial.getId());
        } else if (contatoDoUsuario != null && contatoDoUsuario.getId() != null
                && isContatoNoGrupo(membros, contatoDoUsuario.getId())) {
            candidato = localizarContatoPorId(membros, contatoDoUsuario.getId());
        }

       if (candidato != null && !representaOwner(candidato, ownerId, ownerEmail)) {
            boolean podeTransferir = permitirTransferencia && localizarUsuarioDoContato(candidato).isPresent();
            if (!podeTransferir) {
                candidato = null;
            }
        }

        if (candidato == null) {
            candidato = membros.stream()
                    .filter(Objects::nonNull)
                    .filter(contato -> representaOwner(contato, ownerId, ownerEmail))
                    .sorted(contatoPrioridadeComparator())
                    .findFirst()
                    .orElse(null);
        }

        if (candidato == null && permitirTransferencia) {
            candidato = membros.stream()
                    .filter(Objects::nonNull)
                    .filter(contato -> localizarUsuarioDoContato(contato).isPresent())
                    .sorted(contatoPrioridadeComparator())
                    .findFirst()
                    .orElse(null);
        }

        grupo.setAdminContatoId(candidato != null ? candidato.getId() : null);

        if (candidato == null) {
            return null;
        }

        return candidato;
    }

    private Contato selecionarNovoOwner(Grupo grupo) {
        if (grupo == null || grupo.getMembros() == null) {
            return null;
        }

        return grupo.getMembros().stream()
                .filter(Objects::nonNull)
                .filter(contato -> localizarUsuarioDoContato(contato).isPresent())
                .sorted(contatoPrioridadeComparator())
                .findFirst()
                .orElse(null);
    }

    private Contato localizarContatoPorId(List<Contato> membros, Long contatoId) {
        if (contatoId == null) {
            return null;
        }
        return membros.stream()
                .filter(Objects::nonNull)
                .filter(contato -> contatoId.equals(contato.getId()))
                .findFirst()
                .orElse(null);
    }

    private boolean representaOwner(Contato contato, Long ownerId, String ownerEmail) {
        if (contato == null || ownerId == null) {
            return false;
        }
        if (ownerId.equals(contato.getIdContato())) {
            return true;
        }
        return ownerEmail != null && !ownerEmail.isBlank()
                && ownerEmail.equalsIgnoreCase(contato.getEmail());
    }

    private boolean isContatoNoGrupo(List<Contato> membros, Long contatoId) {
        if (contatoId == null) {
            return false;
        }
        return membros.stream()
                .filter(Objects::nonNull)
                .anyMatch(contato -> contatoId.equals(contato.getId()));
    }

    private Comparator<Contato> contatoPrioridadeComparator() {
        return Comparator
                .comparingLong(this::prioridadePorUsuario)
                .thenComparingLong(this::prioridadePorRegistro)
                .thenComparing(contato -> contato.getNome() != null ? contato.getNome().toLowerCase() : "");
    }

    private long prioridadePorUsuario(Contato contato) {
        if (contato == null) {
            return Long.MAX_VALUE;
        }
        Long idContato = contato.getIdContato();
        return idContato != null ? idContato : Long.MAX_VALUE;
    }

    private long prioridadePorRegistro(Contato contato) {
        if (contato == null) {
            return Long.MAX_VALUE;
        }
        Long id = contato.getId();
        return id != null ? id : Long.MAX_VALUE;
    }


    private Contato localizarContatoDoOwner(Grupo grupo) {
        if (grupo == null || grupo.getMembros() == null || grupo.getOwnerId() == null) {
            return null;
        }

        Long ownerId = grupo.getOwnerId();

        Optional<Contato> contatoPorVinculoDireto = grupo.getMembros().stream()
                .filter(Objects::nonNull)
                .filter(contato -> Objects.equals(ownerId, contato.getIdContato()))
                .findFirst();
        if (contatoPorVinculoDireto.isPresent()) {
            return contatoPorVinculoDireto.get();
        }

        Optional<Usuario> owner = usuarioRepository.findById(ownerId);
        if (owner.isEmpty()) {
            return null;
        }

        String emailOwner = owner.get().getEmail();
        if (emailOwner != null && !emailOwner.isBlank()) {
            Optional<Contato> contatoPorEmail = grupo.getMembros().stream()
                    .filter(Objects::nonNull)
                    .filter(contato -> emailOwner.equalsIgnoreCase(contato.getEmail()))
                    .findFirst();
            if (contatoPorEmail.isPresent()) {
                return contatoPorEmail.get();
            }
        }

        return grupo.getMembros().stream()
                .filter(Objects::nonNull)
                .filter(contato -> Objects.equals(ownerId, contato.getOwnerId())
                        && contato.getIdContato() == null)
                .findFirst()
                .orElse(null);
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

    private boolean atualizarOwnerPorAdmin(Grupo grupo, Contato administrador) {
        if (grupo == null || administrador == null) {
            return false;
        }

        Optional<Usuario> usuarioNovoOwner = localizarUsuarioDoContato(administrador);

        Long novoOwnerId = usuarioNovoOwner.map(Usuario::getId)
                .orElseGet(administrador::getIdContato);

        if (novoOwnerId == null) {
            return false;
        }

        boolean ownerAlterado = !Objects.equals(grupo.getOwnerId(), novoOwnerId);
        grupo.setOwnerId(novoOwnerId);
        if (!Objects.equals(administrador.getIdContato(), novoOwnerId)) {
            administrador.setIdContato(novoOwnerId);
        }
        return ownerAlterado;
    }

    private Optional<Usuario> localizarUsuarioDoContato(Contato contato) {
        if (contato == null) {
            return Optional.empty();
        }

        Long contatoUsuarioId = contato.getIdContato();
        if (contatoUsuarioId != null) {
            Optional<Usuario> porId = usuarioRepository.findById(contatoUsuarioId);
            if (porId.isPresent()) {
                return porId;
            }
        }

        String email = contato.getEmail();
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        return usuarioRepository.findByEmailIgnoreCase(email);
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
            Contato contatoExistente = existente.get();
            if (contatoExistente.getIdContato() == null) {
                contatoExistente.setIdContato(ownerId);
                contatoExistente = contatoRepository.save(contatoExistente);
            }
            return contatoExistente;
        }
        Contato contato = new Contato();
        contato.setOwnerId(ownerId);
        contato.setNome(usuario.getNomeUsuario() != null ? usuario.getNomeUsuario() : "Usuário " + ownerId);
        contato.setEmail(email);
        contato.setPendente(Boolean.FALSE);
        contato.setIdContato(ownerId);
        return contatoRepository.save(contato);
    }

    private Long requireUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("Usuário autenticado é obrigatório");
        }
        return usuarioId;
    }
}