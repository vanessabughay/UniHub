package com.unihub.backend.dto.planejamento;

import com.unihub.backend.model.ColunaPlanejamento;
import com.unihub.backend.model.Contato;
import com.unihub.backend.model.enums.QuadroStatus;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public class QuadroPlanejamentoDetalhesResponse {

    private Long id;
    private String nome;
    private QuadroStatus estado;
    private Instant dataInicio;
    private Instant dataFim;
    private String disciplina;
    private List<String> integrantes;
    private Long donoId;
    private Set<Contato> membros;
    private List<ColunaPlanejamento> colunasEmAndamento;
    private List<ColunaPlanejamento> colunasConcluidas;

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
        this.integrantes = integrantes;
    }

    public Long getDonoId() {
        return donoId;
    }

    public void setDonoId(Long donoId) {
     
   this.donoId = donoId;

    }

    public Set<Contato> getMembros() {
        return membros;
    }

    public void setMembros(Set<Contato> membros) {
        this.membros = membros;
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