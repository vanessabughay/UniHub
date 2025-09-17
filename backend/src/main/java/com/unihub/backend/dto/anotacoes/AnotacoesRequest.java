package com.unihub.backend.dto.anotacoes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AnotacoesRequest {
    @NotBlank
    @Size(max = 500)
    private String titulo;

    private String conteudo;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }
}