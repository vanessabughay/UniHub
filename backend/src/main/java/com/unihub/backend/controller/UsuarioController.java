package com.unihub.backend.controller;

import com.unihub.backend.dto.UpdateUsuarioRequest;
import com.unihub.backend.dto.compartilhamento.NotificacaoResponse;
import com.unihub.backend.dto.compartilhamento.UsuarioResumoResponse;
import com.unihub.backend.model.Contato;
import com.unihub.backend.repository.ContatoRepository;
import com.unihub.backend.repository.UsuarioRepository;
import com.unihub.backend.service.UsuarioExclusaoService;
import com.unihub.backend.service.CompartilhamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping({"/usuarios", "/api/usuarios"})
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ContatoRepository contatoRepository;

    @Autowired
    private CompartilhamentoService compartilhamentoService;

    @Autowired
    private UsuarioExclusaoService usuarioExclusaoService;

    @PutMapping("/me")
    public ResponseEntity<?> atualizarUsuario(@RequestBody UpdateUsuarioRequest request,
                                              @AuthenticationPrincipal Long usuarioId) {
        return repository.findById(usuarioId)
                .map(usuario -> {
                    boolean emailAtualizado = false;
                    String emailAnterior = usuario.getEmail();
                
                    if (request.getName() != null && !request.getName().isBlank()) {
                        usuario.setNomeUsuario(request.getName());
                    }
                    if (request.getEmail() != null && !request.getEmail().isBlank()) {
                        String novoEmail = request.getEmail().trim();
                        if (emailAnterior == null || !emailAnterior.equalsIgnoreCase(novoEmail)) {
                            usuario.setEmail(novoEmail);
                            emailAtualizado = true;
                        }
                    }
                    if (request.getPassword() != null && !request.getPassword().isBlank()) {
                        usuario.setSenha(passwordEncoder.encode(request.getPassword()));
                    }
                    repository.save(usuario);
                    if (emailAtualizado) {
                        atualizarEmailContatos(usuario.getId(), emailAnterior, usuario.getEmail());
                    }
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
 @DeleteMapping("/me")
    public ResponseEntity<Void> excluirUsuario(@AuthenticationPrincipal Long usuarioId) {
        usuarioExclusaoService.excluirUsuario(usuarioId);
        return ResponseEntity.noContent().build();
    }

    private void atualizarEmailContatos(Long usuarioId, String emailAnterior, String novoEmail) {
        if (novoEmail == null || novoEmail.isBlank()) {
            return;
        }

        Set<Long> idsAtualizados = new HashSet<>();
        List<Contato> contatosParaSalvar = new ArrayList<>();

        contatoRepository.findByIdContato(usuarioId).forEach(contato -> {
            atualizarEmailContato(contato, novoEmail, idsAtualizados, contatosParaSalvar);
        });

        if (emailAnterior != null && !emailAnterior.equalsIgnoreCase(novoEmail)) {
            contatoRepository.findByEmailIgnoreCase(emailAnterior).forEach(contato -> {
                atualizarEmailContato(contato, novoEmail, idsAtualizados, contatosParaSalvar);
            });
        }

        if (!contatosParaSalvar.isEmpty()) {
            contatoRepository.saveAll(contatosParaSalvar);
        }
    }
    
    @GetMapping("/{usuarioId}/contatos")
    public List<UsuarioResumoResponse> listarContatosCompartilhamento(@PathVariable Long usuarioId,
                                                                      @AuthenticationPrincipal Long usuarioAutenticado) {
        validarUsuarioAutenticado(usuarioId, usuarioAutenticado);
        return compartilhamentoService.listarContatos(usuarioId);
    }

    @GetMapping("/{usuarioId}/notificacoes")
    public List<NotificacaoResponse> listarNotificacoes(@PathVariable Long usuarioId,
                                                        @AuthenticationPrincipal Long usuarioAutenticado) {
        validarUsuarioAutenticado(usuarioId, usuarioAutenticado);
        return compartilhamentoService.listarNotificacoes(usuarioId);
    }

    private void validarUsuarioAutenticado(Long usuarioId, Long usuarioAutenticado) {
        if (usuarioAutenticado == null || !usuarioAutenticado.equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem acesso");
        }
    }

    private void atualizarEmailContato(Contato contato, String novoEmail, Set<Long> idsAtualizados,
                                       List<Contato> contatosParaSalvar) {
        Long contatoId = contato.getId();
        if (contatoId != null && idsAtualizados.contains(contatoId)) {
            return;
        }

        contato.setEmail(novoEmail);
        contatosParaSalvar.add(contato);

        if (contatoId != null) {
            idsAtualizados.add(contatoId);
        }
    }
}