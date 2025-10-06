package com.unihub.backend.dto.planejamento;

import com.unihub.backend.model.enums.QuadroStatus;

// Este DTO contém apenas os campos que o usuário pode editar ou criar no formulário.
public class QuadroPlanejamentoRequest {

    private String nome;
    private QuadroStatus estado;
    private Long dataFim;
    private Long disciplinaId;
    private Long contatoId;
    private Long grupoId;

    // Getters e Setters para todos os campos
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public QuadroStatus getEstado() { return estado; }
    public void setEstado(QuadroStatus estado) { this.estado = estado; }
    public Long getDataFim() { return dataFim; }
    public void setDataFim(Long dataFim) { this.dataFim = dataFim; }
    public Long getDisciplinaId() { return disciplinaId; }
    public void setDisciplinaId(Long disciplinaId) { this.disciplinaId = disciplinaId; }
    public Long getContatoId() { return contatoId; }
    public void setContatoId(Long contatoId) { this.contatoId = contatoId; }
    public Long getGrupoId() { return grupoId; }
    public void setGrupoId(Long grupoId) { this.grupoId = grupoId; }
}