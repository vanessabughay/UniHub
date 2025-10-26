package com.unihub.backend.repository;

import com.unihub.backend.model.ConviteCompartilhamento;
import com.unihub.backend.model.StatusConvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConviteCompartilhamentoRepository extends JpaRepository<ConviteCompartilhamento, Long> {

    List<ConviteCompartilhamento> findByDestinatarioIdAndStatus(Long destinatarioId, StatusConvite status);

    Optional<ConviteCompartilhamento> findByDisciplinaIdAndDestinatarioIdAndStatus(Long disciplinaId, Long destinatarioId, StatusConvite status);
}
