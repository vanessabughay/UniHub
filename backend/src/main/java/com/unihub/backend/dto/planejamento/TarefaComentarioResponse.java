package com.unihub.backend.dto.planejamento;

public class TarefaComentarioResponse {

    private Long id;
    private String conteudo;
    private Long autorId;
    private String autorNome;
    private boolean autor;
    private Long dataCriacao;
    private Long dataAtualizacao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public Long getAutorId() {
        return autorId;
    }

    public void setAutorId(Long autorId) {
        this.autorId = autorId;
    }

    public String getAutorNome() {
        return autorNome;
    }

    public void setAutorNome(String autorNome) {
        this.autorNome = autorNome;
    }

    public boolean isAutor() {
        return autor;
    }

    public void setAutor(boolean autor) {
        this.autor = autor;
    }

    public Long getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Long dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Long getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(Long dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}