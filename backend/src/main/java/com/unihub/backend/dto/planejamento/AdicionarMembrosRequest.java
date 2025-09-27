package com.unihub.backend.dto.planejamento;

import java.util.List;

public class AdicionarMembrosRequest {

    private List<Long> contatosIds;

    public List<Long> getContatosIds() {
        return contatosIds;
    }

    public void setContatosIds(List<Long> contatosIds) {
        this.contatosIds = contatosIds;
    }
}