package com.unihub.backend.dto.compartilhamento;

import com.unihub.backend.model.ConviteCompartilhamento;
import com.unihub.backend.model.enums.StatusConviteCompartilhamento;

import java.time.LocalDateTime;

public class ConviteCompartilhamentoResponse {

    private Long id;
    private Long disciplinaId;
    private Long remetenteId;
    private Long destinatarioId;
    private StatusConviteCompartilhamento status;
    private String mensagem;
    private LocalDateTime criadoEm;
    private LocalDateTime respondidoEm;

    public static ConviteCompartilhamentoResponse fromEntity(ConviteCompartilhamento convite) {
        ConviteCompartilhamentoResponse response = new ConviteCompartilhamentoResponse();
        response.setId(convite.getId());
        response.setDisciplinaId(convite.getDisciplina() != null ? convite.getDisciplina().getId() : null);
        response.setRemetenteId(convite.getRemetente() != null ? convite.getRemetente().getId() : null);
        response.setDestinatarioId(convite.getDestinatario() != null ? convite.getDestinatario().getId() : null);
        response.setStatus(convite.getStatus());
        response.setMensagem(convite.getMensagem());
        response.setCriadoEm(convite.getCriadoEm());
        response.setRespondidoEm(convite.getRespondidoEm());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public StatusConviteCompartilhamento getStatus() {
        return status;
    }

    public void setStatus(StatusConviteCompartilhamento status) {
        this.status = status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getRespondidoEm() {
        return respondidoEm;
    }

    public void setRespondidoEm(LocalDateTime respondidoEm) {
        this.respondidoEm = respondidoEm;
    }
}