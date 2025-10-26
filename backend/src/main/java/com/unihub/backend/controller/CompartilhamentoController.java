package com.unihub.backend.controller;

import com.unihub.backend.model.ConviteCompartilhamento;
import com.unihub.backend.model.StatusConvite;
import com.unihub.backend.service.CompartilhamentoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/compartilhamentos")
@CrossOrigin(origins = "*")
public class CompartilhamentoController {

    private final CompartilhamentoService compartilhamentoService;

    public CompartilhamentoController(CompartilhamentoService compartilhamentoService) {
        this.compartilhamentoService = compartilhamentoService;
    }

    @PostMapping("/convites")
    public ConviteResponse compartilhar(@RequestBody CompartilharDisciplinaRequest request) {
        ConviteCompartilhamento convite = compartilhamentoService.compartilharDisciplina(
                request.getDisciplinaId(),
                request.getRemetenteId(),
                request.getDestinatarioId(),
                request.getMensagem()
        );
        return ConviteResponse.from(convite);
    }

    @PostMapping("/convites/{conviteId}/aceitar")
    public ConviteResponse aceitar(@PathVariable Long conviteId, @RequestBody AcaoConviteRequest request) {
        ConviteCompartilhamento convite = compartilhamentoService.aceitarConvite(conviteId, request.getUsuarioId());
        return ConviteResponse.from(convite);
    }

    @PostMapping("/convites/{conviteId}/rejeitar")
    public ConviteResponse rejeitar(@PathVariable Long conviteId, @RequestBody AcaoConviteRequest request) {
        ConviteCompartilhamento convite = compartilhamentoService.rejeitarConvite(conviteId, request.getUsuarioId());
        return ConviteResponse.from(convite);
    }

    public static class CompartilharDisciplinaRequest {
        private Long disciplinaId;
        private Long remetenteId;
        private Long destinatarioId;
        private String mensagem;

        public Long getDisciplinaId() {
            return disciplinaId;
        }

        public void setDisciplinaId(Long disciplinaId) {
            this.disciplinaId = disciplinaId;
        }

        public Long getRemetenteId() {
            return remetenteId;
        }

        public void setRemetenteId(Long remetenteId) {
            this.remetenteId = remetenteId;
        }

        public Long getDestinatarioId() {
            return destinatarioId;
        }

        public void setDestinatarioId(Long destinatarioId) {
            this.destinatarioId = destinatarioId;
        }

        public String getMensagem() {
            return mensagem;
        }

        public void setMensagem(String mensagem) {
            this.mensagem = mensagem;
        }
    }

    public static class AcaoConviteRequest {
        private Long usuarioId;

        public Long getUsuarioId() {
            return usuarioId;
        }

        public void setUsuarioId(Long usuarioId) {
            this.usuarioId = usuarioId;
        }
    }

    public static class ConviteResponse {
        private Long id;
        private Long disciplinaId;
        private Long remetenteId;
        private Long destinatarioId;
        private String status;
        private String mensagem;

        public static ConviteResponse from(ConviteCompartilhamento convite) {
            ConviteResponse response = new ConviteResponse();
            response.id = convite.getId();
            response.disciplinaId = convite.getDisciplina() != null ? convite.getDisciplina().getId() : null;
            response.remetenteId = convite.getRemetente() != null ? convite.getRemetente().getId() : null;
            response.destinatarioId = convite.getDestinatario() != null ? convite.getDestinatario().getId() : null;
            response.status = convite.getStatus() != null ? convite.getStatus().name() : StatusConvite.PENDENTE.name();
            response.mensagem = convite.getMensagem();
            return response;
        }

        public Long getId() {
            return id;
        }

        public Long getDisciplinaId() {
            return disciplinaId;
        }

        public Long getRemetenteId() {
            return remetenteId;
        }

        public Long getDestinatarioId() {
            return destinatarioId;
        }

        public String getStatus() {
            return status;
        }

        public String getMensagem() {
            return mensagem;
        }
    }
}
