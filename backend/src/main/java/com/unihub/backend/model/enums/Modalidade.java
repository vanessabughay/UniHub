package com.unihub.backend.model.enums;


public enum Modalidade {
    INDIVIDUAL("INDIVIDUAL"),
    EM_GRUPO("EM GRUPO");

    private final String displayName;

    Modalidade(String displayName) {
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