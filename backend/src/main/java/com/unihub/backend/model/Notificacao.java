package com.unihub.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacoes")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "convite_id")
    private ConviteCompartilhamento convite;

     @Column(length = 150)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String mensagem;

    @Column(nullable = false)
    private boolean lida = false;

    @Column(length = 100)
    private String tipo;

    @Column(length = 100)
    private String categoria;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(nullable = false)
    private boolean interacaoPendente = false;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;


    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    @Column(name = "atualizada_em", nullable = false)
    private LocalDateTime atualizadaEm;

    public Notificacao() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public ConviteCompartilhamento getConvite() {
        return convite;
    }

    public void setConvite(ConviteCompartilhamento convite) {
        this.convite = convite;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }


    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public boolean isLida() {
        return lida;
    }

    public void setLida(boolean lida) {
        this.lida = lida;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Long getReferenciaId() {
        return referenciaId;
    }

    public void setReferenciaId(Long referenciaId) {
        this.referenciaId = referenciaId;
    }

    public boolean isInteracaoPendente() {
        return interacaoPendente;
    }

    public void setInteracaoPendente(boolean interacaoPendente) {
        this.interacaoPendente = interacaoPendente;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }

    
    public LocalDateTime getAtualizadaEm() {
        return atualizadaEm;
    }

    public void setAtualizadaEm(LocalDateTime atualizadaEm) {
        this.atualizadaEm = atualizadaEm;
    }
}