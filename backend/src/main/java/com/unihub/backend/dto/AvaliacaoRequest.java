package com.unihub.backend.dto;

import com.unihub.backend.model.enums.EstadoAvaliacao;
import com.unihub.backend.model.enums.Modalidade;
import com.unihub.backend.model.enums.Prioridade;

public record AvaliacaoRequest(
        Long id,
        String descricao,
        DisciplinaRef disciplina,
        String tipoAvaliacao,
        Modalidade modalidade,
        Prioridade prioridade,
        Boolean receberNotificacoes,
        String dataEntrega,      // use String ISO (AAAA-MM-DD) ou mude para LocalDate
        Double nota,
        Double peso,
        java.util.List<ContatoRef> integrantes,

        EstadoAvaliacao estado,
        Integer dificuldade
) {}