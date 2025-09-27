package com.unihub.backend.model.enums;

public enum Prioridade {
    MUITO_BAIXA("MUITO BAIXA"),
    BAIXA("BAIXA"),
    MEDIA("MEDIA"),
    ALTA("ALTA"),    MUITO_ALTA("MUITO ALTA");

    private final String displayName;

    Prioridade(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName; // Opcional
    }
}
