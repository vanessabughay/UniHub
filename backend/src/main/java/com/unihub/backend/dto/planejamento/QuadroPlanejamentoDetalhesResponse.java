package com.unihub.backend.dto.planejamento;

import com.unihub.backend.model.ColunaPlanejamento;
import com.unihub.backend.model.enums.QuadroStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public class QuadroPlanejamentoDetalhesResponse {

    private Long id;
    private String nome;
    private QuadroStatus estado;
    private LocalDateTime dataFim;
    private Long donoId;

    // os campos antigos pelos novos IDs
    private Long disciplinaId;
    private Long contatoId;
    private Long grupoId;

    // Campos para detalhes
    private List<ColunaPlanejamento> colunasEmAndamento;
    private List<ColunaPlanejamento> colunasConcluidas;

    public QuadroPlanejamentoDetalhesResponse() {
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

    public List<ColunaPlanejamento> getColunasEmAndamento() {
        return colunasEmAndamento;
    }

    public void setColunasEmAndamento(List<ColunaPlanejamento> colunasEmAndamento) {
        this.colunasEmAndamento = colunasEmAndamento;
    }

    public List<ColunaPlanejamento> getColunasConcluidas() {
        return colunasConcluidas;
    }

    public void setColunasConcluidas(List<ColunaPlanejamento> colunasConcluidas) {
        this.colunasConcluidas = colunasConcluidas;
    }
}