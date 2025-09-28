package com.unihub.backend.dto.planejamento;

import com.unihub.backend.model.QuadroPlanejamento;
import com.unihub.backend.model.enums.QuadroStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class QuadroPlanejamentoListaResponse {

    private Long id;
    private String nome;
    private QuadroStatus estado;
    private Instant dataInicio;
    private Instant dataFim;
    private String disciplina;
    private List<String> integrantes;
    private Long donoId;

    public static QuadroPlanejamentoListaResponse fromEntity(QuadroPlanejamento quadro) {
        QuadroPlanejamentoListaResponse response = new QuadroPlanejamentoListaResponse();
        response.setId(quadro.getId());
        response.setNome(quadro.getTitulo());
        response.setEstado(quadro.getStatus());
        response.setDataInicio(quadro.getDataCriacao());
        response.setDataFim(quadro.getDataPrazo());
        response.setDisciplina(quadro.getDisciplina());
        response.setIntegrantes(quadro.getIntegrantes());
        response.setDonoId(quadro.getDonoId());
        return response;
    }

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

    public Instant getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Instant dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Instant getDataFim() {
        return dataFim;
    }

    public void setDataFim(Instant dataFim) {
        this.dataFim = dataFim;
    }

    public String getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(String disciplina) {
        this.disciplina = disciplina;
    }

    public List<String> getIntegrantes() {
        return integrantes;
    }

    public void setIntegrantes(List<String> integrantes) {
        this.integrantes = integrantes != null ? new ArrayList<>(integrantes) : new ArrayList<>();
    }

    public Long getDonoId() {
        return donoId;
    }

    public void setDonoId(Long donoId) {
        this.donoId = donoId;
    }
}