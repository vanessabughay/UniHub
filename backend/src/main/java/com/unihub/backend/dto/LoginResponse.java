package com.unihub.backend.dto;

public class LoginResponse {
    private String token;
    private String nomeUsuario;
    private String email;
    private Long usuarioId;
    private boolean googleCalendarLinked;

    public LoginResponse(String token, String nomeUsuario, String email, Long usuarioId, boolean googleCalendarLinked) {
        this.token = token;
        this.nomeUsuario = nomeUsuario;
        this.email = email;
        this.usuarioId = usuarioId;
        this.googleCalendarLinked = googleCalendarLinked;
    }

    public String getToken() {
        return token;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public String getEmail() {
        return email;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }
    
    public boolean isGoogleCalendarLinked() {
        return googleCalendarLinked;
    }
}