package com.unihub.backend.dto;

public class LoginResponse {
    private String token;
    private String nomeUsuario;

    public LoginResponse(String token, String nomeUsuario) {
        this.token = token;
        this.nomeUsuario = nomeUsuario;
    }

    public String getToken() {
        return token;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }
}