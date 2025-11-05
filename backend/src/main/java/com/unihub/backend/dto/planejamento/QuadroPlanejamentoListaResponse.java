package com.unihub.backend.dto.planejamento;

import com.unihub.backend.model.QuadroPlanejamento;
import com.unihub.backend.model.enums.QuadroStatus;

import java.time.Instant;
import java.time.LocalDateTime;


public class QuadroPlanejamentoListaResponse {

    private Long id;
    private String nome;
    private QuadroStatus estado;
    private LocalDateTime dataFim;
    private Long donoId;

    private Long disciplinaId;
    private Long contatoId;
    private Long grupoId;

    public static QuadroPlanejamentoListaResponse fromEntity(QuadroPlanejamento quadro) {
        QuadroPlanejamentoListaResponse response = new QuadroPlanejamentoListaResponse();
        response.setId(quadro.getId());
        response.setNome(quadro.getTitulo());
        response.setEstado(quadro.getStatus());
        response.setDataFim(quadro.getDataPrazo());
        response.setDonoId(quadro.getDonoId());

        // Lógica atualizada para extrair os IDs das relações
        if (quadro.getDisciplina() != null) {
            response.setDisciplinaId(quadro.getDisciplina().getId());
        }
        if (quadro.getContato() != null) {
            response.setContatoId(quadro.getContato().getIdContato());
                }
        if (quadro.getGrupo() != null) {
            response.setGrupoId(quadro.getGrupo().getId());
        }

        return response;
    }

    // --- GETTERS E SETTERS COMPLETOS ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public QuadroStatus getEstado() {
        return estado;
    }

    public void setEstado(QuadroStatus estado) {
        this.estado = estado;
    }

    
    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public Long getDonoId() {
        return donoId;
    }

    public void setDonoId(Long donoId) {
        this.donoId = donoId;
    }

    public Long getDisciplinaId() {
        return disciplinaId;
    }

    public void setDisciplinaId(Long disciplinaId) {
        this.disciplinaId = disciplinaId;
    }

    public Long getContatoId() {
        return contatoId;
    }

    public void setContatoId(Long contatoId) {
        this.contatoId = contatoId;
    }

    public Long getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(Long grupoId) {
        this.grupoId = grupoId;
    }
}