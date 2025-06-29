package com.unihub.backend.repository;

import com.unihub.backend.model.Disciplina;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {
    List<Disciplina> findByNomeContainingIgnoreCase(String nome);
    
}
