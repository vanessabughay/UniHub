package com.unihub.backend.dto;

public class LoginResponse {
    private String token;
    private String nomeUsuario;
        private Long usuarioId;

    public LoginResponse(String token, String nomeUsuario, Long usuarioId) {
        this.token = token;
        this.nomeUsuario = nomeUsuario;
        this.usuarioId = usuarioId;
    }

    public String getToken() {
        return token;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }
}