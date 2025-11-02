package com.unihub.backend.service;

import com.unihub.backend.model.Contato;
import com.unihub.backend.repository.ContatoRepository;
import com.unihub.backend.repository.UsuarioRepository;
import com.unihub.backend.model.Usuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
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
                contato.setDataSolicitacao(LocalDateTime.now());
            }
            if (Boolean.TRUE.equals(contato.getPendente())) {
                contato.setDataConfirmacao(null);
            } else if (contato.getDataConfirmacao() == null) {
                contato.setDataConfirmacao(LocalDateTime.now());
            }
        }
        Long ownerId = currentUserId();
        contato.setOwnerId(ownerId);

        boolean novoContato = contato.getId() == null;
        String emailNormalizado = contato.getEmail() != null ? contato.getEmail().trim() : null;
        boolean deveEnviarConvite = false;
        String nomeRemetente = null;

        if (novoContato && emailNormalizado != null && !emailNormalizado.isBlank()) {
            if (usuarioRepository.findByEmailIgnoreCase(emailNormalizado).isEmpty()) {
                contato.setEmail(emailNormalizado);
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
        }

        return salvo;
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
        if (!repository.existsByIdAndOwnerId(id, currentUserId()))
            throw new RuntimeException("Sem acesso");
        repository.deleteById(id);
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
        convite.setNome(usuarioAtual.getNomeUsuario());
        convite.setEmail(usuarioAtual.getEmail());
        convite.setIdContato(usuarioAtual.getId());
        convite.setDataConfirmacao(LocalDateTime.now());
        repository.save(convite);

      

        Long ownerId = convite.getOwnerId();
        if (ownerId != null) {
            Optional<Usuario> donoOptional = usuarioRepository.findById(ownerId);
            if (donoOptional.isPresent()) {
                Usuario dono = donoOptional.get();
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
                        LocalDateTime agora = LocalDateTime.now();
                        novoContato.setDataSolicitacao(agora);
                        novoContato.setDataConfirmacao(agora);
                        repository.save(novoContato);
                    }
                }
            }
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

}
