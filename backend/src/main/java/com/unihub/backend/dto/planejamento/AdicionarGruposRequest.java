package com.unihub.backend.dto.planejamento;

import java.util.List;

public class AdicionarGruposRequest {

    private List<Long> gruposIds;

    public List<Long> getGruposIds() {
        return gruposIds;
    }

    public void setGruposIds(List<Long> gruposIds) {
        this.gruposIds = gruposIds;
    }
}