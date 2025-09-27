package com.unihub.backend.model.enums;

public enum EstadoAvaliacao {
    A_REALIZAR("A REALIZAR"),
    EM_ANDAMENTO("EM ANDAMENTO"),
    CONCLUIDA("CONCLUÍDA");

    private final String displayName;

    EstadoAvaliacao(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName; // Opcional: para que o enum.toString() retorne o displayName
    }
}