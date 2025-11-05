package com.unihub.backend.model.enums;

public enum Antecedencia {
    NO_DIA(0),
    UM_DIA(1),
    DOIS_DIAS(2),
    TRES_DIAS(3),
    UMA_SEMANA(7);

    private final int dias;

    Antecedencia(int dias) {
        this.dias = dias;
    }

    public int getDias() {
        return dias;
    }

    public static Antecedencia padrao() {
        return NO_DIA;
    }
}