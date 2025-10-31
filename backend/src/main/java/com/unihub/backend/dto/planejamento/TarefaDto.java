package com.unihub.backend.dto.planejamento;

// DTO que o app (TelaInicial) espera receber
public class TarefaDto {

    private String titulo;
    private Long dataPrazo;
    private String nomeQuadro;

    // Construtor
    public TarefaDto(String titulo, Long dataPrazo, String nomeQuadro) {
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

    public Long getDataPrazo() {
                return dataPrazo;
    }

    public void setDataPrazo(Long dataPrazo) {
                this.dataPrazo = dataPrazo;
    }

    public String getNomeQuadro() {
        return nomeQuadro;
    }

    public void setNomeQuadro(String nomeQuadro) {
        this.nomeQuadro = nomeQuadro;
    }
}