package com.unihub.backend.dto.anotacoes;

import java.time.LocalDateTime;

public class AnotacoesResponse {
    private Long id;
    private String titulo;
    private String conteudo;
    private Long disciplinaId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AnotacoesResponse(Long id, String titulo, String conteudo,
                            Long disciplinaId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.disciplinaId = disciplinaId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // getters
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getConteudo() { return conteudo; }
    public Long getDisciplinaId() { return disciplinaId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}