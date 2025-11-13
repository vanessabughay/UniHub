package com.unihub.backend.dto;

public class LoginResponse {
    private String token;
    private String nomeUsuario;
    private String email;
    private Long usuarioId;
    private boolean googleCalendarLinked;
     private boolean hasInstitution;

    public LoginResponse(String token, String nomeUsuario, String email, Long usuarioId,
                         boolean googleCalendarLinked, boolean hasInstitution) {
        this.token = token;
        this.nomeUsuario = nomeUsuario;
        this.email = email;
        this.usuarioId = usuarioId;
        this.googleCalendarLinked = googleCalendarLinked;
        this.hasInstitution = hasInstitution;
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

    public boolean hasInstitution() {
        return hasInstitution;
    }

    public boolean isHasInstitution() {
        return hasInstitution;
    }
}