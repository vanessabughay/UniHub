package com.unihub.backend.dto.planejamento;

// DTO que o app (TelaInicial) espera receber
public class TarefaDto {

    private String titulo;
    private String dataPrazo; 
    private String nomeQuadro;

    public TarefaDto() {
    }

    // Construtor
    public TarefaDto(String titulo, String dataPrazo, String nomeQuadro) {
        this.titulo = titulo;
        this.dataPrazo = dataPrazo;
        this.nomeQuadro = nomeQuadro;
    }

    // Getters e Setters
    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDataPrazo() {
        return dataPrazo;
    }

    public void setDataPrazo(String dataPrazo) {
        this.dataPrazo = dataPrazo;
    }

    public String getNomeQuadro() {
        return nomeQuadro;
    }

    public void setNomeQuadro(String nomeQuadro) {
        this.nomeQuadro = nomeQuadro;
    }
}