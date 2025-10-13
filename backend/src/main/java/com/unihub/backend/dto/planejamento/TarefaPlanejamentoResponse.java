package com.unihub.backend.dto.planejamento;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.unihub.backend.model.ColunaPlanejamento;
import com.unihub.backend.model.Contato;
import com.unihub.backend.model.TarefaPlanejamento;
import com.unihub.backend.model.enums.TarefaStatus;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TarefaPlanejamentoResponse {

    private Long id;
    private String nome;
    private String descricao;
    private LocalDate prazo;
    private TarefaStatus estado;
    private Long colunaId;
    private Long quadroId;
    private Long responsavelId;
    private String responsavelNome;

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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getPrazo() {
        return prazo;
    }

    public void setPrazo(LocalDate prazo) {
        this.prazo = prazo;
    }

    public TarefaStatus getEstado() {
        return estado;
    }

    public void setEstado(TarefaStatus estado) {
        this.estado = estado;
    }

    public Long getColunaId() {
        return colunaId;
    }

    public void setColunaId(Long colunaId) {
        this.colunaId = colunaId;
    }

    public Long getQuadroId() {
        return quadroId;
    }

    public void setQuadroId(Long quadroId) {
        this.quadroId = quadroId;
    }

    public Long getResponsavelId() {
        return responsavelId;
    }

    public void setResponsavelId(Long responsavelId) {
        this.responsavelId = responsavelId;
    }

    public String getResponsavelNome() {
        return responsavelNome;
    }

    public void setResponsavelNome(String responsavelNome) {
        this.responsavelNome = responsavelNome;
    }

    public static TarefaPlanejamentoResponse fromEntity(TarefaPlanejamento tarefa) {
        TarefaPlanejamentoResponse response = new TarefaPlanejamentoResponse();
        response.setId(tarefa.getId());
        response.setNome(tarefa.getTitulo());
        response.setDescricao(tarefa.getDescricao());
        response.setPrazo(tarefa.getDataPrazo());
        response.setEstado(tarefa.getStatus());

        ColunaPlanejamento coluna = tarefa.getColuna();
        if (coluna != null) {
            response.setColunaId(coluna.getId());
            if (coluna.getQuadro() != null) {
                response.setQuadroId(coluna.getQuadro().getId());
            }
        }

        Contato responsavel = tarefa.getResponsavel();
        if (responsavel != null) {
            response.setResponsavelId(responsavel.getId());
            response.setResponsavelNome(responsavel.getNome());
        }

        return response;
    }
}