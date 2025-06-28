package com.unihub.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Disciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String professor;
    private String periodo;
    private int cargaHoraria;
    private LocalDate dataInicioSemestre;
    private LocalDate dataFimSemestre;
    private String emailProfessor;
    private String plataforma;
    private String telefoneProfessor;
    private String salaProfessor;
    private boolean isAtiva;
    private boolean receberNotificacoes;
    @OneToMany(mappedBy = "disciplina", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HorarioAula> aulas;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getProfessor() { return professor; }
    public void setProfessor(String professor) { this.professor = professor; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public int getCargaHoraria() { return cargaHoraria; }
    public void setCargaHoraria(int cargaHoraria) { this.cargaHoraria = cargaHoraria; }

    public LocalDate getDataInicioSemestre() { return dataInicioSemestre; }
    public void setDataInicioSemestre(LocalDate dataInicioSemestre) { this.dataInicioSemestre = dataInicioSemestre; }

    public LocalDate getDataFimSemestre() { return dataFimSemestre; }
    public void setDataFimSemestre(LocalDate dataFimSemestre) { this.dataFimSemestre = dataFimSemestre; }

    public String getEmailProfessor() { return emailProfessor; }
    public void setEmailProfessor(String emailProfessor) { this.emailProfessor = emailProfessor; }

    public String getPlataforma() { return plataforma; }
    public void setPlataforma(String plataforma) { this.plataforma = plataforma; }

    public String getTelefoneProfessor() { return telefoneProfessor; }
    public void setTelefoneProfessor(String telefoneProfessor) { this.telefoneProfessor = telefoneProfessor; }

    public String getSalaProfessor() { return salaProfessor; }
    public void setSalaProfessor(String salaProfessor) { this.salaProfessor = salaProfessor; }

    public boolean isAtiva() { return isAtiva; }
    public void setAtiva(boolean ativa) { isAtiva = ativa; }

    public boolean isReceberNotificacoes() { return receberNotificacoes; }
    public void setReceberNotificacoes(boolean receberNotificacoes) { this.receberNotificacoes = receberNotificacoes; }

    public List<HorarioAula> getAulas() { return aulas; }
    public void setAulas(List<HorarioAula> aulas) {
        this.aulas = aulas;
        if (aulas != null) {
            for (HorarioAula aula : aulas) {
                aula.setDisciplina(this);
            }
        }
    }
}
