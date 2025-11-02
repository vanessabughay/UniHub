package com.unihub.backend.service;

import com.unihub.backend.model.Contato;
import com.unihub.backend.model.QuadroPlanejamento;
import com.unihub.backend.model.TarefaPlanejamento;
import com.unihub.backend.model.Usuario;
import com.unihub.backend.repository.ContatoRepository;
import com.unihub.backend.repository.ConviteCompartilhamentoRepository;
import com.unihub.backend.repository.NotificacaoRepository;
import com.unihub.backend.repository.QuadroPlanejamentoRepository;
import com.unihub.backend.repository.TarefaComentarioNotificacaoRepository;
import com.unihub.backend.repository.TarefaComentarioRepository;
import com.unihub.backend.repository.TarefaPlanejamentoRepository;
import com.unihub.backend.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class UsuarioExclusaoService {

    private final UsuarioRepository usuarioRepository;
    private final ContatoRepository contatoRepository;
    private final QuadroPlanejamentoRepository quadroRepository;
    private final TarefaPlanejamentoRepository tarefaRepository;
    private final TarefaComentarioRepository tarefaComentarioRepository;
    private final TarefaComentarioNotificacaoRepository tarefaComentarioNotificacaoRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final ConviteCompartilhamentoRepository conviteRepository;
    private final GrupoService grupoService;

    public UsuarioExclusaoService(UsuarioRepository usuarioRepository,
                                  ContatoRepository contatoRepository,
                                  QuadroPlanejamentoRepository quadroRepository,
                                  TarefaPlanejamentoRepository tarefaRepository,
                                  TarefaComentarioRepository tarefaComentarioRepository,
                                  TarefaComentarioNotificacaoRepository tarefaComentarioNotificacaoRepository,
                                  NotificacaoRepository notificacaoRepository,
                                  ConviteCompartilhamentoRepository conviteRepository,
                                  GrupoService grupoService) {
        this.usuarioRepository = usuarioRepository;
        this.contatoRepository = contatoRepository;
        this.quadroRepository = quadroRepository;
        this.tarefaRepository = tarefaRepository;
        this.tarefaComentarioRepository = tarefaComentarioRepository;
        this.tarefaComentarioNotificacaoRepository = tarefaComentarioNotificacaoRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.conviteRepository = conviteRepository;
        this.grupoService = grupoService;
    }

    @Transactional
    public void excluirUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("Usuário autenticado é obrigatório");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        removerParticipacoesEmTarefas(usuario);
        tarefaComentarioRepository.deleteByAutorId(usuarioId);
        tarefaComentarioNotificacaoRepository.deleteByUsuarioId(usuarioId);

        grupoService.processarRemocaoUsuario(usuario);
        reatribuirQuadros(usuario);

        conviteRepository.deleteByRemetenteIdOrDestinatarioId(usuarioId, usuarioId);
        notificacaoRepository.deleteByUsuarioId(usuarioId);

        removerContatos(usuario);

        usuarioRepository.delete(usuario);
    }

    private void removerParticipacoesEmTarefas(Usuario usuario) {
        Long usuarioId = usuario.getId();

        tarefaRepository.findDistinctByResponsaveis_IdContato(usuarioId).forEach(tarefa -> {
            if (removerResponsaveis(tarefa, contato -> Objects.equals(contato.getIdContato(), usuarioId))) {
                tarefaRepository.save(tarefa);
            }
        });

        List<Contato> contatosRelacionados = coletarContatosParaRemocao(usuario);
        contatosRelacionados.stream()
                .map(Contato::getId)
                .filter(Objects::nonNull)
                .forEach(contatoId -> tarefaRepository.findDistinctByResponsaveis_Id(contatoId).forEach(tarefa -> {
                    if (removerResponsaveis(tarefa, contato -> Objects.equals(contato.getId(), contatoId))) {
                        tarefaRepository.save(tarefa);
                    }
                }));
    }

    private void reatribuirQuadros(Usuario usuario) {
        Long usuarioId = usuario.getId();

        List<QuadroPlanejamento> quadros = quadroRepository.findByUsuarioId(usuarioId);
        for (QuadroPlanejamento quadro : quadros) {
            Optional<Usuario> novoOwner = escolherNovoOwner(quadro, usuarioId);
            if (novoOwner.isEmpty()) {
                quadroRepository.delete(quadro);
                continue;
            }

            quadro.setUsuario(novoOwner.get());
            Contato contatoAtual = quadro.getContato();
            if (contatoAtual != null && Objects.equals(contatoAtual.getOwnerId(), usuarioId)) {
                quadro.setContato(null);
            }
            quadroRepository.save(quadro);
        }
    }

    private Optional<Usuario> escolherNovoOwner(QuadroPlanejamento quadro, Long usuarioAtual) {
        Map<Long, Usuario> candidatos = new LinkedHashMap<>();

        if (quadro.getContato() != null) {
            localizarUsuarioDoContato(quadro.getContato())
                    .filter(usuario -> !Objects.equals(usuario.getId(), usuarioAtual))
                    .ifPresent(usuario -> candidatos.putIfAbsent(usuario.getId(), usuario));
        }

        if (quadro.getGrupo() != null) {
            Long ownerId = quadro.getGrupo().getOwnerId();
            if (ownerId != null && !Objects.equals(ownerId, usuarioAtual)) {
                usuarioRepository.findById(ownerId)
                        .ifPresent(usuario -> candidatos.putIfAbsent(usuario.getId(), usuario));
            }

            if (quadro.getGrupo().getMembros() != null) {
                quadro.getGrupo().getMembros().stream()
                        .filter(Objects::nonNull)
                        .map(this::localizarUsuarioDoContato)
                        .flatMap(Optional::stream)
                        .filter(usuario -> !Objects.equals(usuario.getId(), usuarioAtual))
                        .forEach(usuario -> candidatos.putIfAbsent(usuario.getId(), usuario));
            
            }
        }
        
       return candidatos.values().stream().findFirst();
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

    private void removerContatos(Usuario usuario) {
        List<Contato> contatos = coletarContatosParaRemocao(usuario);
        if (!contatos.isEmpty()) {
            contatoRepository.deleteAll(contatos);
        }
    }

    private List<Contato> coletarContatosParaRemocao(Usuario usuario) {
        Long usuarioId = usuario.getId();
        Set<Long> idsAdicionados = new LinkedHashSet<>();
        List<Contato> contatos = new ArrayList<>();

        contatoRepository.findByOwnerId(usuarioId).forEach(contato -> adicionarContato(contatos, idsAdicionados, contato));
        contatoRepository.findByIdContato(usuarioId).forEach(contato -> adicionarContato(contatos, idsAdicionados, contato));

        return contatos;
    }

    private void adicionarContato(List<Contato> contatos, Set<Long> idsAdicionados, Contato contato) {
        if (contato == null) {
            return;
        }
        Long id = contato.getId();
        if (id != null) {
            if (idsAdicionados.add(id)) {
                contatos.add(contato);
            }
        } else if (!contatos.contains(contato)) {
            contatos.add(contato);
        }
    }

    private boolean removerResponsaveis(TarefaPlanejamento tarefa, Predicate<Contato> filtro) {
        if (tarefa.getResponsaveis() == null || tarefa.getResponsaveis().isEmpty()) {
            return false;
        }

        LinkedHashSet<Contato> filtrados = tarefa.getResponsaveis().stream()
                .filter(filtro.negate())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (filtrados.size() == tarefa.getResponsaveis().size()) {
            return false;
        }

        tarefa.setResponsaveis(filtrados);
        return true;
    }
}