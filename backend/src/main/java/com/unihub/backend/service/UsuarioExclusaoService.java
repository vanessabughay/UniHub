package com.unihub.backend.service;

import com.unihub.backend.model.Contato;
import com.unihub.backend.model.Grupo;
import com.unihub.backend.model.QuadroPlanejamento;
import com.unihub.backend.model.TarefaPlanejamento;
import com.unihub.backend.model.Usuario;
import com.unihub.backend.repository.ContatoRepository;
import com.unihub.backend.repository.ConviteCompartilhamentoRepository;
import com.unihub.backend.repository.NotificacaoRepository;
import com.unihub.backend.repository.QuadroPlanejamentoRepository;
import com.unihub.backend.repository.TarefaNotificacaoRepository;
import com.unihub.backend.repository.TarefaComentarioRepository;
import com.unihub.backend.repository.TarefaPlanejamentoRepository;
import com.unihub.backend.repository.UsuarioRepository;
import com.unihub.backend.repository.GrupoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class UsuarioExclusaoService {

    private final UsuarioRepository usuarioRepository;
    private final ContatoRepository contatoRepository;
    private final QuadroPlanejamentoRepository quadroRepository;
    private final TarefaPlanejamentoRepository tarefaRepository;
    private final TarefaComentarioRepository tarefaComentarioRepository;
    private final TarefaNotificacaoRepository tarefaComentarioNotificacaoRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final ConviteCompartilhamentoRepository conviteRepository;
    private final GrupoService grupoService;
    private final GrupoRepository grupoRepository;

    public UsuarioExclusaoService(UsuarioRepository usuarioRepository,
                                  ContatoRepository contatoRepository,
                                  QuadroPlanejamentoRepository quadroRepository,
                                  TarefaPlanejamentoRepository tarefaRepository,
                                  TarefaComentarioRepository tarefaComentarioRepository,
                                  TarefaNotificacaoRepository tarefaComentarioNotificacaoRepository,
                                  NotificacaoRepository notificacaoRepository,
                                  ConviteCompartilhamentoRepository conviteRepository,
                                  GrupoService grupoService,
                                  GrupoRepository grupoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.contatoRepository = contatoRepository;
        this.quadroRepository = quadroRepository;
        this.tarefaRepository = tarefaRepository;
        this.tarefaComentarioRepository = tarefaComentarioRepository;
        this.tarefaComentarioNotificacaoRepository = tarefaComentarioNotificacaoRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.conviteRepository = conviteRepository;
        this.grupoService = grupoService;
        this.grupoRepository = grupoRepository;
    }

    @Transactional
    public void excluirUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("Usuário autenticado é obrigatório");
        }

        // carrega o usuário COM a lista de quadros (porque ela é o lado forte do orphanRemoval)
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        // 1) remover o usuário das tarefas
        removerParticipacoesEmTarefas(usuario);

        // 2) reatribuir quadros ENQUANTO ainda temos contatos e grupos
        //    esse método devolve os IDs de quadros que foram transferidos
        Set<Long> quadrosTransferidos = reatribuirQuadros(usuario);

        // 3) IMPORTANTÍSSIMO: tirar esses quadros da lista do usuário
        //    por causa do cascade = ALL + orphanRemoval = true
        if (usuario.getQuadrosPlanejamento() != null && !usuario.getQuadrosPlanejamento().isEmpty()) {
            usuario.getQuadrosPlanejamento()
                    .removeIf(q -> q != null && quadrosTransferidos.contains(q.getId()));
        }

        // 4) agora podemos deixar o GrupoService fazer a faxina dele
        grupoService.processarRemocaoUsuario(usuario);

        // 5) apagar comentários e notificações de comentários
        tarefaComentarioRepository.deleteByAutorId(usuarioId);
        tarefaComentarioNotificacaoRepository.deleteByUsuarioId(usuarioId);

        // 6) apagar convites e notificações
        conviteRepository.deleteByRemetenteIdOrDestinatarioId(usuarioId, usuarioId);
        notificacaoRepository.deleteByUsuarioId(usuarioId);

        // 7) apagar contatos ligados ao usuário
        removerContatos(usuario);

        // 8) por fim, apagar o usuário
        usuarioRepository.delete(usuario);
    }

    /**
     * Remove o usuário (e contatos dele) de tarefas onde ele aparece como responsável.
     */
    private void removerParticipacoesEmTarefas(Usuario usuario) {
        Long usuarioId = usuario.getId();

        // tarefas em que o responsável é diretamente o usuário
        tarefaRepository.findDistinctByResponsaveis_IdContato(usuarioId).forEach(tarefa -> {
            if (removerResponsaveis(tarefa, c -> Objects.equals(c.getIdContato(), usuarioId))) {
                tarefaRepository.save(tarefa);
            }
        });

        // tarefas em que o responsável é algum contato que pertence a esse usuário
        List<Contato> contatosDoUsuario = coletarContatosParaRemocao(usuario);
        contatosDoUsuario.stream()
                .map(Contato::getId)
                .filter(Objects::nonNull)
                .forEach(contatoId -> tarefaRepository.findDistinctByResponsaveis_Id(contatoId).forEach(tarefa -> {
                    if (removerResponsaveis(tarefa, c -> Objects.equals(c.getId(), contatoId))) {
                        tarefaRepository.save(tarefa);
                    }
                }));
    }

    /**
     * Reatribui ou apaga os quadros do usuário e devolve os IDs que foram transferidos
     * para outro usuário (precisamos disso para remover da lista do usuário).
     */
    private Set<Long> reatribuirQuadros(Usuario usuario) {
        Long usuarioId = usuario.getId();
        Set<Long> quadrosTransferidos = new HashSet<>();

        // pega todos os quadros onde ele é dono
        List<QuadroPlanejamento> quadros = quadroRepository.findByUsuarioId(usuarioId);
        for (QuadroPlanejamento quadro : quadros) {

            // tenta achar outro dono
            Optional<Usuario> novoOwner = escolherNovoOwner(quadro, usuarioId);

            if (novoOwner.isPresent()) {
                quadro.setUsuario(novoOwner.get());

                // se o contato do quadro apontava pro usuário que saiu, a gente limpa
                Contato contatoAtual = carregarContato(quadro.getContato());
                if (contatoAtual != null &&
                        (Objects.equals(contatoAtual.getIdContato(), usuarioId)
                                || Objects.equals(contatoAtual.getOwnerId(), usuarioId))) {
                    quadro.setContato(null);
                }

                quadroRepository.save(quadro);
                quadrosTransferidos.add(quadro.getId());
                continue;
            }

            // não achou outro dono → posso apagar?
            boolean temOutrosIntegrantes = quadroTemOutrosIntegrantes(quadro, usuarioId);

            if (!temOutrosIntegrantes) {
                // não sobrou ninguém → apaga mesmo
                quadroRepository.delete(quadro);
            } else {
                // sobrou gente mas não consegui converter pra usuario (caso muito específico)
                // tenta pôr no dono do grupo
                Grupo grupo = carregarGrupo(quadro.getGrupo());
                if (grupo != null &&
                        grupo.getOwnerId() != null &&
                        !Objects.equals(grupo.getOwnerId(), usuarioId)) {

                    usuarioRepository.findById(grupo.getOwnerId()).ifPresent(donoGrupo -> {
                        quadro.setUsuario(donoGrupo);
                        quadroRepository.save(quadro);
                        quadrosTransferidos.add(quadro.getId());
                    });

                } else {
                    // mantém vivo sem dono
                    quadro.setUsuario(null);
                    quadroRepository.save(quadro);
                }
            }
        }

        // garante que o UPDATE dos quadros foi pro banco antes do delete do usuário
        quadroRepository.flush();

        return quadrosTransferidos;
    }

    /**
     * 1. se tem contato no quadro e esse contato aponta pra outro usuário → ele é o novo dono
     * 2. se tem grupo e o grupo tem owner diferente → ele é o novo dono
     * 3. se tem grupo e membros com idContato diferente → pega o primeiro
     */
    private Optional<Usuario> escolherNovoOwner(QuadroPlanejamento quadro, Long usuarioAtual) {
        // 1) contato direto
        Contato contatoQuadro = carregarContato(quadro.getContato());
        if (contatoQuadro != null && contatoQuadro.getIdContato() != null) {
            Long novoId = contatoQuadro.getIdContato();
            if (!Objects.equals(novoId, usuarioAtual)) {
                return usuarioRepository.findById(novoId);
            }
        }

        // 2) grupo
        Grupo grupo = carregarGrupo(quadro.getGrupo());
        if (grupo != null) {
            // 2.1) dono do grupo
            if (grupo.getOwnerId() != null && !Objects.equals(grupo.getOwnerId(), usuarioAtual)) {
                return usuarioRepository.findById(grupo.getOwnerId());
            }

            // 2.2) qualquer membro do grupo com idContato diferente
            if (grupo.getMembros() != null) {
                for (Contato membro : grupo.getMembros()) {
                    Contato m = carregarContato(membro);
                    if (m == null) continue;
                    Long membroUsuarioId = m.getIdContato();
                    if (membroUsuarioId != null && !Objects.equals(membroUsuarioId, usuarioAtual)) {
                        Optional<Usuario> u = usuarioRepository.findById(membroUsuarioId);
                        if (u.isPresent()) {
                            return u;
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Checa se ainda existe alguém no quadro além do usuário que está saindo.
     */
    private boolean quadroTemOutrosIntegrantes(QuadroPlanejamento quadro, Long usuarioQueSaiu) {
        // contato direto
        Contato c = carregarContato(quadro.getContato());
        if (c != null) {
            boolean ehDoSaindo =
                    Objects.equals(c.getIdContato(), usuarioQueSaiu) ||
                    Objects.equals(c.getOwnerId(), usuarioQueSaiu);
            if (!ehDoSaindo) {
                return true;
            }
        }

        // grupo
        Grupo g = carregarGrupo(quadro.getGrupo());
        if (g != null) {
            // owner do grupo
            if (g.getOwnerId() != null && !Objects.equals(g.getOwnerId(), usuarioQueSaiu)) {
                return true;
            }
            // membros
            if (g.getMembros() != null) {
                for (Contato membro : g.getMembros()) {
                    Contato m = carregarContato(membro);
                    if (m == null) continue;
                    if (m.getIdContato() != null && !Objects.equals(m.getIdContato(), usuarioQueSaiu)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private Grupo carregarGrupo(Grupo grupo) {
        if (grupo == null) return null;
        if (grupo.getId() == null) return grupo;
        return grupoRepository.findById(grupo.getId()).orElse(grupo);
    }

    private Contato carregarContato(Contato contato) {
        if (contato == null) return null;
        if (contato.getId() != null) {
            return contatoRepository.findById(contato.getId()).orElse(contato);
        }
        if (contato.getOwnerId() != null && contato.getIdContato() != null) {
            return contatoRepository.findByOwnerIdAndIdContato(contato.getOwnerId(), contato.getIdContato())
                    .orElse(contato);
        }
        return contato;
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

        contatoRepository.findByOwnerId(usuarioId)
                .forEach(contato -> adicionarContato(contatos, idsAdicionados, contato));

        contatoRepository.findByIdContato(usuarioId)
                .forEach(contato -> adicionarContato(contatos, idsAdicionados, contato));

        return contatos;
    }

    private void adicionarContato(List<Contato> contatos, Set<Long> idsAdicionados, Contato contato) {
        if (contato == null) return;
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
