package com.unihub.backend.repository;

import com.unihub.backend.model.ConviteCompartilhamento;
import com.unihub.backend.model.enums.StatusConviteCompartilhamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConviteCompartilhamentoRepository extends JpaRepository<ConviteCompartilhamento, Long> {

    Optional<ConviteCompartilhamento> findByIdAndDestinatarioId(Long id, Long destinatarioId);

    Optional<ConviteCompartilhamento> findByDisciplinaIdAndDestinatarioIdAndStatus(Long disciplinaId,
                                                                                   Long destinatarioId,
                                                                                   StatusConviteCompartilhamento status);

    List<ConviteCompartilhamento> findByDestinatarioIdAndStatus(Long destinatarioId,
                                                                StatusConviteCompartilhamento status);

    void deleteByRemetenteIdOrDestinatarioId(Long remetenteId, Long destinatarioId);
}