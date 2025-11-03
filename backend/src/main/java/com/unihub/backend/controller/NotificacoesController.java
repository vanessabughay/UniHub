package com.unihub.backend.controller;

import com.unihub.backend.dto.notificacoes.NotificacoesConfigRequest;
import com.unihub.backend.dto.notificacoes.NotificacoesConfigResponse;
import com.unihub.backend.service.NotificacaoConfiguracaoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notificacoes")
@CrossOrigin(origins = "*")
public class NotificacoesController {

    private final NotificacaoConfiguracaoService notificacaoConfiguracaoService;

    public NotificacoesController(NotificacaoConfiguracaoService notificacaoConfiguracaoService) {
        this.notificacaoConfiguracaoService = notificacaoConfiguracaoService;
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
}