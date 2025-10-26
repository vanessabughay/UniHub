package com.unihub.backend.controller;

import com.unihub.backend.dto.compartilhamento.AcaoConviteRequest;
import com.unihub.backend.dto.compartilhamento.CompartilharDisciplinaRequest;
import com.unihub.backend.dto.compartilhamento.ConviteCompartilhamentoResponse;
import com.unihub.backend.service.CompartilhamentoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/compartilhamentos")
@CrossOrigin(origins = "*")
public class CompartilhamentoController {

    private final CompartilhamentoService service;

    public CompartilhamentoController(CompartilhamentoService service) {
        this.service = service;
    }

    @PostMapping("/convites")
    public ConviteCompartilhamentoResponse compartilhar(
            @RequestBody CompartilharDisciplinaRequest request,
            @AuthenticationPrincipal Long usuarioId) {
        return service.compartilhar(usuarioId, request);
    }

    @PostMapping("/convites/{conviteId}/aceitar")
    public ConviteCompartilhamentoResponse aceitar(
            @PathVariable Long conviteId,
            @RequestBody AcaoConviteRequest request,
            @AuthenticationPrincipal Long usuarioId) {
        validarUsuario(request.getUsuarioId(), usuarioId);
        return service.aceitarConvite(conviteId, usuarioId);
    }

    @PostMapping("/convites/{conviteId}/rejeitar")
    public ConviteCompartilhamentoResponse rejeitar(
            @PathVariable Long conviteId,
            @RequestBody AcaoConviteRequest request,
            @AuthenticationPrincipal Long usuarioId) {
        validarUsuario(request.getUsuarioId(), usuarioId);
        return service.rejeitarConvite(conviteId, usuarioId);
    }

    private void validarUsuario(Long requestId, Long autenticadoId) {
        if (autenticadoId == null || requestId == null || !Objects.equals(requestId, autenticadoId)) {
            throw new IllegalArgumentException("Usuário não autorizado");
        }
    }
}