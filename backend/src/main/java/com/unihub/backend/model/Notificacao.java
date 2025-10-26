package com.unihub.backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private String mensagem;

    @Enumerated(EnumType.STRING)
    private TipoNotificacao tipo;

    private boolean lida = false;

    private LocalDateTime criadaEm = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "convite_id")
    private ConviteCompartilhamento convite;

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

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public TipoNotificacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoNotificacao tipo) {
        this.tipo = tipo;
    }

    public boolean isLida() {
        return lida;
    }

    public void setLida(boolean lida) {
        this.lida = lida;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }

    public ConviteCompartilhamento getConvite() {
        return convite;
    }

    public void setConvite(ConviteCompartilhamento convite) {
        this.convite = convite;
    }
}
