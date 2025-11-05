package com.unihub.backend.controller;

import com.unihub.backend.dto.compartilhamento.NotificacaoResponse;
import com.unihub.backend.dto.notificacoes.NotificacaoLogRequest;
import com.unihub.backend.dto.notificacoes.NotificacoesConfigRequest;
import com.unihub.backend.dto.notificacoes.NotificacoesConfigResponse;
import com.unihub.backend.service.NotificacaoConfiguracaoService;
import com.unihub.backend.service.NotificacaoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/config")
    public NotificacoesConfigResponse carregar(@AuthenticationPrincipal Long usuarioId) {
        return notificacaoConfiguracaoService.carregar(usuarioId);
    }

    @PutMapping("/config")
    public NotificacoesConfigResponse salvar(@AuthenticationPrincipal Long usuarioId,
                                             @RequestBody NotificacoesConfigRequest request) {
        return notificacaoConfiguracaoService.salvar(usuarioId, request);
    }

    @PostMapping("/historico")
    public NotificacaoResponse registrar(@AuthenticationPrincipal Long usuarioId,
                                         @RequestBody NotificacaoLogRequest request) {
        return notificacaoService.registrarNotificacao(usuarioId, request);
    }
}