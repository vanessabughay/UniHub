package com.unihub.backend.repository;

import com.unihub.backend.model.Ausencia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AusenciaRepository extends JpaRepository<Ausencia, Long> {
}