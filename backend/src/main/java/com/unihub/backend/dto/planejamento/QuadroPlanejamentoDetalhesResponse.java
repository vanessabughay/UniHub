package com.unihub.backend.dto.planejamento;

import com.unihub.backend.model.ColunaPlanejamento;
import com.unihub.backend.model.Contato;
import com.unihub.backend.model.Disciplina;
import com.unihub.backend.model.enums.QuadroStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class QuadroPlanejamentoDetalhesResponse {

    private Long id;
    private String titulo;
    private String descricao;
    private QuadroStatus status;
    private LocalDateTime dataCriacao;
    private LocalDate dataPrazo;
    private LocalDateTime dataEncerramento;
    private Disciplina disciplina;
    private Set<Contato> membros;
    private List<ColunaPlanejamento> colunasEmAndamento;
    private List<ColunaPlanejamento> colunasConcluidas;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public QuadroStatus getStatus() {
        return status;
    }

    public void setStatus(QuadroStatus status) {
        this.status = status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDate getDataPrazo() {
        return dataPrazo;
    }

    public void setDataPrazo(LocalDate dataPrazo) {
        this.dataPrazo = dataPrazo;
    }

    public LocalDateTime getDataEncerramento() {
        return dataEncerramento;
    }

    public void setDataEncerramento(LocalDateTime dataEncerramento) {
        this.dataEncerramento = dataEncerramento;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(Disciplina disciplina) {
        this.disciplina = disciplina;
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