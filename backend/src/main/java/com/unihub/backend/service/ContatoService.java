package com.unihub.backend.service;

import com.unihub.backend.model.Contato;
import com.unihub.backend.repository.ContatoRepository;
import com.unihub.backend.repository.UsuarioRepository;
import com.unihub.backend.repository.NotificacaoRepository;
import com.unihub.backend.model.Usuario;
import com.unihub.backend.model.Notificacao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.HashMap;
import java.util.stream.Collectors;



@Service
public class ContatoService {
    @Autowired
    private ContatoRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    private static final String NOTIFICACAO_TIPO_CONVITE = "CONTATO_SOLICITACAO";
    private static final String NOTIFICACAO_TIPO_RESPOSTA = "CONTATO_SOLICITACAO_RESPOSTA";
    private static final String NOTIFICACAO_CATEGORIA = "CONTATO";
    private static final String NOTIFICACAO_TITULO_CONVITE = "Solicitação de contato";
    private static final String NOTIFICACAO_TITULO_RESPOSTA = "Atualização de contato";
    private static final ZoneId ZONA_BRASIL = ZoneId.of("America/Sao_Paulo");

     @Autowired
    private InvitationEmailService invitationEmailService;

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        return (auth.getPrincipal() instanceof Long)
                ? (Long) auth.getPrincipal()
                : Long.valueOf(auth.getName());
    }

    public List<Contato> listarTodas() {
        return repository.findByOwnerId(currentUserId());
    }

    public List<Contato> buscarPendentesPorEmail(String email) {
        if (email == null || email.isBlank()) {
            return List.of();
        }

        Long ownerId = currentUserId();

       List<Contato> convitesPendentes = repository.findByEmailIgnoreCaseAndPendenteTrue(email.trim());

        Set<Long> ownerIds = convitesPendentes.stream()
                .map(Contato::getOwnerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Usuario> owners = new HashMap<>();
        usuarioRepository.findAllById(ownerIds)
                .forEach(usuario -> owners.put(usuario.getId(), usuario));

        return convitesPendentes.stream()
                .filter(contato -> ownerId == null || !ownerId.equals(contato.getOwnerId()))
                .map(contato -> mapearConviteParaDono(contato, owners.get(contato.getOwnerId())))
                .collect(Collectors.toList());
    }



    public Contato salvar(Contato contato) {
        if (contato.getId() == null) {
            contato.setIdContato(null);
            if (contato.getDataSolicitacao() == null) {
                contato.setDataSolicitacao(agora());
            }
            if (Boolean.TRUE.equals(contato.getPendente())) {
                contato.setDataConfirmacao(null);
            } else if (contato.getDataConfirmacao() == null) {
                contato.setDataSolicitacao(agora());
            }
            } else {
            repository.findById(contato.getId())
                    .map(Contato::getEmail)
                    .ifPresent(contato::setEmail);
        }
        Long ownerId = currentUserId();
        contato.setOwnerId(ownerId);

        boolean novoContato = contato.getId() == null;
        String emailNormalizado = contato.getEmail() != null ? contato.getEmail().trim() : null;
        boolean deveEnviarConvite = false;
        String nomeRemetente = null;
        Usuario destinatarioExistente = null;

        if (novoContato && emailNormalizado != null && !emailNormalizado.isBlank()) {
            contato.setEmail(emailNormalizado);
            Optional<Usuario> usuarioDestino = usuarioRepository.findByEmailIgnoreCase(emailNormalizado);
            if (usuarioDestino.isPresent()) {
                destinatarioExistente = usuarioDestino.get();
            } else {
                deveEnviarConvite = true;
                if (ownerId != null) {
                    nomeRemetente = usuarioRepository.findById(ownerId)
                            .map(Usuario::getNomeUsuario)
                            .orElse(null);
                }
            }
        }

        Contato salvo = repository.save(contato);

        if (deveEnviarConvite) {
            invitationEmailService.enviarConvite(emailNormalizado, nomeRemetente);
            } else if (destinatarioExistente != null
                && !Objects.equals(ownerId, destinatarioExistente.getId())
                && Boolean.TRUE.equals(salvo.getPendente())) {
            Usuario remetente = ownerId != null ? usuarioRepository.findById(ownerId).orElse(null) : null;
            criarOuAtualizarNotificacaoConvite(salvo, destinatarioExistente, remetente);
        }

        return salvo;
    }

     public void processarConvitesPendentesParaUsuario(Usuario usuario) {
        if (usuario == null) {
            return;
        }

        Long usuarioId = usuario.getId();
        String email = Optional.ofNullable(usuario.getEmail())
                .map(String::trim)
                .orElse("");

        if (usuarioId == null || email.isEmpty()) {
            return;
        }

        List<Contato> convitesPendentes = repository.findByEmailIgnoreCaseAndPendenteTrue(email);
        if (convitesPendentes.isEmpty()) {
            return;
        }

        Map<Long, Usuario> donosConvite = new HashMap<>();

        for (Contato convite : convitesPendentes) {
            convite.setIdContato(usuarioId);
            convite.setNome(usuario.getNomeUsuario());
            convite.setEmail(email);

            Long ownerId = convite.getOwnerId();
            if (ownerId != null && !donosConvite.containsKey(ownerId)) {
                usuarioRepository.findById(ownerId)
                        .ifPresent(dono -> donosConvite.put(ownerId, dono));
            }
        }

        repository.saveAll(convitesPendentes);

        for (Contato convite : convitesPendentes) {
            Usuario remetente = convite.getOwnerId() != null ? donosConvite.get(convite.getOwnerId()) : null;
            criarOuAtualizarNotificacaoConvite(convite, usuario, remetente);
        }
    }
    
    private Contato mapearConviteParaDono(Contato conviteOriginal, Usuario dono) {
        Contato resposta = new Contato();
        resposta.setId(conviteOriginal.getId());
        resposta.setPendente(conviteOriginal.getPendente());
        resposta.setOwnerId(conviteOriginal.getOwnerId());
        resposta.setIdContato(conviteOriginal.getIdContato());
        resposta.setDataSolicitacao(conviteOriginal.getDataSolicitacao());
        resposta.setDataConfirmacao(conviteOriginal.getDataConfirmacao());

        if (dono != null) {
            resposta.setNome(dono.getNomeUsuario());
            resposta.setEmail(dono.getEmail());
        } else {
            resposta.setNome(conviteOriginal.getNome());
            resposta.setEmail(conviteOriginal.getEmail());
        }

        return resposta;
    }

    public Contato buscarPorId(Long id) {
        return repository.findByIdAndOwnerId(id, currentUserId())
                .orElseThrow(() -> new RuntimeException("Contato não encontrado"));
    }

    public void excluir(Long id) {
        Long ownerId = currentUserId();
        Contato contato = repository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new RuntimeException("Sem acesso"));

        repository.delete(contato);

        Long contatoUsuarioId = contato.getIdContato();
        if (contatoUsuarioId != null) {
            repository.findByOwnerIdAndIdContato(contatoUsuarioId, ownerId)
                    .ifPresent(contatoReciproco -> {
                        if (!Objects.equals(contatoReciproco.getId(), contato.getId())) {
                            repository.delete(contatoReciproco);
                        }
                    });
        }
    }

    
    public void aceitarConvite(Long conviteId) {
        Long usuarioId = currentUserId();
        if (usuarioId == null) {
            throw new RuntimeException("Usuário não autenticado");
        }

        Usuario usuarioAtual = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Contato convite = repository.findById(conviteId)
                .orElseThrow(() -> new RuntimeException("Convite não encontrado"));

        if (!Boolean.TRUE.equals(convite.getPendente())) {
            throw new RuntimeException("Convite já respondido");
        }

        if (convite.getEmail() == null || !convite.getEmail().equalsIgnoreCase(usuarioAtual.getEmail())) {
            throw new RuntimeException("Sem acesso");
        }

        convite.setPendente(false);
        if (convite.getNome() == null || convite.getNome().isBlank()) {
            convite.setNome(usuarioAtual.getNomeUsuario());
        }
        convite.setEmail(usuarioAtual.getEmail());
        convite.setIdContato(usuarioAtual.getId());
        convite.setDataConfirmacao(agora());
        repository.save(convite);

      

        Long ownerId = convite.getOwnerId();
        Usuario donoConvite = ownerId != null ? usuarioRepository.findById(ownerId).orElse(null) : null;
        if (donoConvite != null) {
            Usuario dono = donoConvite;
            String emailDono = dono.getEmail();
            if (emailDono != null) {
                boolean jaExiste = repository.findByOwnerIdAndEmailIgnoreCase(usuarioAtual.getId(), emailDono)
                        .isPresent();

                if (!jaExiste) {
                    Contato novoContato = new Contato();
                    novoContato.setOwnerId(usuarioAtual.getId());
                    novoContato.setNome(dono.getNomeUsuario());
                    novoContato.setEmail(emailDono);
                    novoContato.setPendente(false);
                    novoContato.setIdContato(dono.getId());
                    LocalDateTime momentoAtual = agora();
                    novoContato.setDataSolicitacao(momentoAtual);
                    novoContato.setDataConfirmacao(momentoAtual);
                    repository.save(novoContato);
                }
            }
        }
        
        atualizarNotificacaoConviteParaUsuario(convite, usuarioAtual, donoConvite, true);
        if (donoConvite != null) {
            notificarRespostaParaDono(convite, donoConvite, usuarioAtual, true);
        }
        
    }

    public void rejeitarConvite(Long conviteId) {
        Long usuarioId = currentUserId();
        if (usuarioId == null) {
            throw new RuntimeException("Usuário não autenticado");
        }

        Usuario usuarioAtual = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Contato convite = repository.findById(conviteId)
                .orElseThrow(() -> new RuntimeException("Convite não encontrado"));

        if (convite.getEmail() == null || !convite.getEmail().equalsIgnoreCase(usuarioAtual.getEmail())) {
            throw new RuntimeException("Sem acesso");
        }

        if (!Boolean.TRUE.equals(convite.getPendente())) {
            throw new RuntimeException("Convite já respondido");
        }

        Long ownerId = convite.getOwnerId();
        Usuario donoConvite = ownerId != null ? usuarioRepository.findById(ownerId).orElse(null) : null;

        atualizarNotificacaoConviteParaUsuario(convite, usuarioAtual, donoConvite, false);
        if (donoConvite != null) {
            notificarRespostaParaDono(convite, donoConvite, usuarioAtual, false);
        }

        repository.deleteById(conviteId);
    }

    public List<Contato> buscarPorNome(String nome) {
        Long ownerId = currentUserId();
        if (ownerId == null) {
            return List.of();
        }

        List<Contato> contatos = repository.findByOwnerId(ownerId);

        if (nome == null || nome.isBlank()) {
            return contatos;
        }

        String termoNormalizado = nome.toLowerCase();
        return contatos.stream()
                .filter(
                        contato ->
                                contato.getNome() != null
                                        && contato.getNome().toLowerCase().contains(termoNormalizado))
                .collect(Collectors.toList());
    }
private void criarOuAtualizarNotificacaoConvite(Contato convite, Usuario destinatario, Usuario remetente) {
        if (convite == null || destinatario == null) {
            return;
        }

        Long referenciaId = convite.getId();
        Long destinatarioId = destinatario.getId();
        if (referenciaId == null || destinatarioId == null) {
            return;
        }

        Notificacao notificacao = notificacaoRepository
                .findByUsuarioIdAndTipoAndCategoriaAndReferenciaId(destinatarioId,
                        NOTIFICACAO_TIPO_CONVITE, NOTIFICACAO_CATEGORIA, referenciaId)
                .orElseGet(Notificacao::new);

        boolean nova = notificacao.getId() == null;
        notificacao.setUsuario(destinatario);
        notificacao.setTitulo(NOTIFICACAO_TITULO_CONVITE);
        String nomeRemetente = resolverNomeUsuario(remetente, "Um contato");
        String mensagem = String.format("%s quer adicionar você como contato.", nomeRemetente);
        notificacao.setMensagem(mensagem);
        notificacao.setTipo(NOTIFICACAO_TIPO_CONVITE);
        notificacao.setCategoria(NOTIFICACAO_CATEGORIA);
        notificacao.setReferenciaId(referenciaId);
        notificacao.setInteracaoPendente(true);
        notificacao.setLida(false);
        notificacao.setConvite(null);
        LocalDateTime momentoAtual = agora();
        if (nova) {
            notificacao.setCriadaEm(momentoAtual);
        }
        notificacao.setAtualizadaEm(momentoAtual);
        notificacaoRepository.save(notificacao);
    }

    private void atualizarNotificacaoConviteParaUsuario(Contato convite,
                                                        Usuario usuario,
                                                        Usuario donoConvite,
                                                        boolean aceito) {
        if (convite == null || usuario == null) {
            return;
        }

        Long referenciaId = convite.getId();
        Long usuarioId = usuario.getId();
        if (referenciaId == null || usuarioId == null) {
            return;
        }

        notificacaoRepository
                .findByUsuarioIdAndTipoAndCategoriaAndReferenciaId(usuarioId,
                        NOTIFICACAO_TIPO_CONVITE, NOTIFICACAO_CATEGORIA, referenciaId)
                .ifPresent(notificacao -> {
                    String nomeDono = resolverNomeUsuario(donoConvite, "seu contato");
                    String mensagem = aceito
                            ? String.format("Você aceitou a solicitação de contato de %s.", nomeDono)
                            : String.format("Você rejeitou a solicitação de contato de %s.", nomeDono);
                    notificacao.setMensagem(mensagem);
                    notificacao.setTitulo(NOTIFICACAO_TITULO_CONVITE);
                    notificacao.setLida(true);
                    notificacao.setInteracaoPendente(false);
                    notificacao.setAtualizadaEm(agora());
                    notificacaoRepository.save(notificacao);
                });
    }

    private void notificarRespostaParaDono(Contato convite,
                                           Usuario dono,
                                           Usuario respondente,
                                           boolean aceito) {
        if (convite == null || dono == null) {
            return;
        }

        Long referenciaId = convite.getId();
        Long donoId = dono.getId();
        if (referenciaId == null || donoId == null) {
            return;
        }

        Notificacao notificacao = notificacaoRepository
                .findByUsuarioIdAndTipoAndCategoriaAndReferenciaId(donoId,
                        NOTIFICACAO_TIPO_RESPOSTA, NOTIFICACAO_CATEGORIA, referenciaId)
                .orElseGet(Notificacao::new);

        boolean nova = notificacao.getId() == null;
        notificacao.setUsuario(dono);
        notificacao.setTitulo(NOTIFICACAO_TITULO_RESPOSTA);
        String nomeRespondente = resolverNomeUsuario(respondente, "Seu contato");
        String mensagem = aceito
                ? String.format("%s aceitou sua solicitação de contato.", nomeRespondente)
                : String.format("%s rejeitou sua solicitação de contato.", nomeRespondente);
        notificacao.setMensagem(mensagem);
        notificacao.setTipo(NOTIFICACAO_TIPO_RESPOSTA);
        notificacao.setCategoria(NOTIFICACAO_CATEGORIA);
        notificacao.setReferenciaId(referenciaId);
        notificacao.setInteracaoPendente(false);
        notificacao.setConvite(null);
         LocalDateTime momentoAtual = agora();
        if (nova) {
            notificacao.setCriadaEm(momentoAtual);
            notificacao.setLida(false);
        }
        notificacao.setAtualizadaEm(momentoAtual);
        notificacaoRepository.save(notificacao);
    }

    private String resolverNomeUsuario(Usuario usuario, String fallbackPadrao) {
        if (usuario == null) {
            return fallbackPadrao;
        }
        String nome = usuario.getNomeUsuario();
        if (nome != null && !nome.isBlank()) {
            return nome;
        }
        String email = usuario.getEmail();
        if (email != null && !email.isBlank()) {
            return email;
        }
        return fallbackPadrao;
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(ZONA_BRASIL);
    }
}
