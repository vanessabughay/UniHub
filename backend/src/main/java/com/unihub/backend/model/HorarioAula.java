package com.unihub.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

@Entity
public class HorarioAula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String diaDaSemana;
    private String sala;
    private String horarioInicio;
    private String horarioFim;

    @ManyToOne
    @JoinColumn(name = "disciplina_id")
    @JsonBackReference
    private Disciplina disciplina;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDiaDaSemana() { return diaDaSemana; }
    public void setDiaDaSemana(String diaDaSemana) { this.diaDaSemana = diaDaSemana; }

    public String getSala() { return sala; }
    public void setSala(String sala) { this.sala = sala; }

    public String getHorarioInicio() { return horarioInicio; }
    public void setHorarioInicio(String horarioInicio) { this.horarioInicio = horarioInicio; }

    public String getHorarioFim() { return horarioFim; }
    public void setHorarioFim(String horarioFim) { this.horarioFim = horarioFim; }

    public Disciplina getDisciplina() { return disciplina; }
    public void setDisciplina(Disciplina disciplina) { this.disciplina = disciplina; }
}
