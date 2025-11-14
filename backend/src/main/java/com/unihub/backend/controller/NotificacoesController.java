package com.unihub.backend.controller;

import com.unihub.backend.dto.compartilhamento.NotificacaoResponse;
import com.unihub.backend.dto.notificacoes.NotificacaoLogRequest;
import com.unihub.backend.dto.notificacoes.NotificacoesConfigRequest;
import com.unihub.backend.dto.notificacoes.NotificacoesConfigResponse;
import com.unihub.backend.service.NotificacaoConfiguracaoService;
import com.unihub.backend.service.NotificacaoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/notificacoes")
@CrossOrigin(origins = "*")
public class NotificacoesController {

    private final NotificacaoConfiguracaoService notificacaoConfiguracaoService;
    private final NotificacaoService notificacaoService;

    public NotificacoesController(NotificacaoConfiguracaoService notificacaoConfiguracaoService,
                                  NotificacaoService notificacaoService) {
        this.notificacaoConfiguracaoService = notificacaoConfiguracaoService;
        this.notificacaoService = notificacaoService;
    }

    @GetMapping({"/config", "/usuarios/{usuarioId}/notificacoes-config"})
    public NotificacoesConfigResponse carregar(@AuthenticationPrincipal Long usuarioAutenticado,
                                               @PathVariable(value = "usuarioId", required = false) Long usuarioId) {
        Long resolvedId = resolveUsuarioId(usuarioId, usuarioAutenticado);
        return notificacaoConfiguracaoService.carregar(resolvedId);
    }

    @PutMapping({"/config", "/usuarios/{usuarioId}/notificacoes-config"})
    public NotificacoesConfigResponse salvar(@AuthenticationPrincipal Long usuarioAutenticado,
                                             @PathVariable(value = "usuarioId", required = false) Long usuarioId,
                                             @RequestBody NotificacoesConfigRequest request) {
        Long resolvedId = resolveUsuarioId(usuarioId, usuarioAutenticado);
        return notificacaoConfiguracaoService.salvar(resolvedId, request);
    }

    @GetMapping({"/historico", "/usuarios/{usuarioId}/historico"})
    public List<NotificacaoResponse> listar(@AuthenticationPrincipal Long usuarioAutenticado,
                                            @PathVariable(value = "usuarioId", required = false) Long usuarioId) {
        Long resolvedId = resolveUsuarioId(usuarioId, usuarioAutenticado);
        return notificacaoService.listarHistorico(resolvedId);
    }

    @PostMapping("/historico")
    public NotificacaoResponse registrar(@AuthenticationPrincipal Long usuarioId,
                                         @RequestBody NotificacaoLogRequest request) {
        return notificacaoService.registrarNotificacao(usuarioId, request);
    }

    private Long resolveUsuarioId(Long pathUsuarioId, Long usuarioAutenticado) {
        Long resolved = (pathUsuarioId != null) ? pathUsuarioId : usuarioAutenticado;

        if (resolved == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }

        if (usuarioAutenticado == null || !resolved.equals(usuarioAutenticado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem acesso");
        }

        return resolved;
    }
}
