package com.unihub.backend.controller;

import com.unihub.backend.model.Contato;
import com.unihub.backend.model.Notificacao;
import com.unihub.backend.model.Usuario;
import com.unihub.backend.service.CompartilhamentoService;
import com.unihub.backend.service.UsuarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final CompartilhamentoService compartilhamentoService;

    public UsuarioController(UsuarioService usuarioService, CompartilhamentoService compartilhamentoService) {
        this.usuarioService = usuarioService;
        this.compartilhamentoService = compartilhamentoService;
    }

    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioService.listarTodos();
    }

    @PostMapping
    public Usuario criarUsuario(@RequestBody Usuario usuario) {
        return usuarioService.salvar(usuario);
    }

    @GetMapping("/{usuarioId}/contatos")
    public List<ContatoResponse> listarContatos(@PathVariable Long usuarioId) {
        return compartilhamentoService.listarContatos(usuarioId).stream()
                .map(usuario -> new ContatoResponse(usuario.getId(), usuario.getNome(), usuario.getEmail()))
                .collect(Collectors.toList());
    }

    @PostMapping("/{usuarioId}/contatos")
    public ContatoResponse adicionarContato(@PathVariable Long usuarioId, @RequestBody AdicionarContatoRequest request) {
        Contato contato = compartilhamentoService.adicionarContato(usuarioId, request.getContatoId());
        Usuario contatoUsuario = contato.getContato();
        return new ContatoResponse(contatoUsuario.getId(), contatoUsuario.getNome(), contatoUsuario.getEmail());
    }

    @GetMapping("/{usuarioId}/notificacoes")
    public List<NotificacaoResponse> listarNotificacoes(@PathVariable Long usuarioId) {
        return compartilhamentoService.listarNotificacoes(usuarioId).stream()
                .map(NotificacaoResponse::from)
                .collect(Collectors.toList());
    }

    public static class ContatoResponse {
        private Long id;
        private String nome;
        private String email;

        public ContatoResponse(Long id, String nome, String email) {
            this.id = id;
            this.nome = nome;
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public String getNome() {
            return nome;
        }

        public String getEmail() {
            return email;
        }
    }

    public static class AdicionarContatoRequest {
        private Long contatoId;

        public Long getContatoId() {
            return contatoId;
        }

        public void setContatoId(Long contatoId) {
            this.contatoId = contatoId;
        }
    }

    public static class NotificacaoResponse {
        private Long id;
        private String mensagem;
        private boolean lida;
        private String tipo;
        private Long conviteId;
        private java.time.LocalDateTime criadaEm;

        public static NotificacaoResponse from(Notificacao notificacao) {
            NotificacaoResponse response = new NotificacaoResponse();
            response.id = notificacao.getId();
            response.mensagem = notificacao.getMensagem();
            response.lida = notificacao.isLida();
            response.tipo = notificacao.getTipo() != null ? notificacao.getTipo().name() : null;
            response.conviteId = notificacao.getConvite() != null ? notificacao.getConvite().getId() : null;
            response.criadaEm = notificacao.getCriadaEm();
            return response;
        }

        public Long getId() {
            return id;
        }

        public String getMensagem() {
            return mensagem;
        }

        public boolean isLida() {
            return lida;
        }

        public String getTipo() {
            return tipo;
        }

        public Long getConviteId() {
            return conviteId;
        }

        public java.time.LocalDateTime getCriadaEm() {
            return criadaEm;
        }
    }
}
