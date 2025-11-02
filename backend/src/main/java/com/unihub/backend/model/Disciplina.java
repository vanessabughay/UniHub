
package com.unihub.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // Para equals e hashCode

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;


// evita expandir essas relações quando Disciplina vier como filha
@JsonIgnoreProperties({
        "hibernateLazyInitializer", "handler",
        "avaliacoes", "usuario"
})

@Entity
public class Disciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;
    private String nome;
    private String professor;
    private String periodo;
    private int cargaHoraria; // Mantido como int primitivo
    private Integer qtdSemanas;
    private LocalDate dataInicioSemestre;
    private LocalDate dataFimSemestre;
    private String emailProfessor;
    private String plataforma;
    private String telefoneProfessor;
    private String salaProfessor;
    private boolean isAtiva; // Mantido como boolean primitivo
    private boolean receberNotificacoes; // Mantido como boolean primitivo
    private Integer ausenciasPermitidas;

    @ManyToOne(fetch = FetchType.LAZY) // LAZY é geralmente uma boa prática
    @JoinColumn(name = "usuario_id")
    @JsonBackReference("usuario-disciplinas") // Referência para evitar loop com Usuario
    private Usuario usuario;

    @OneToMany(
            mappedBy = "disciplina", // Mapeado pelo campo 'disciplina' na entidade Avaliacao
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )


    private List<Avaliacao> avaliacoes = new ArrayList<>();

    @OneToMany(
            mappedBy = "disciplina", // Mapeado pelo campo 'disciplina' na entidade HorarioAula
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )

    @JsonManagedReference("disciplina-aulas") // Gerencia a serialização da lista de aulas

    private List<HorarioAula> aulas = new ArrayList<>();

    @OneToMany(
            mappedBy = "disciplina",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonManagedReference("disciplina-ausencias")
    private List<Ausencia> ausencias = new ArrayList<>();

    // --- Construtor Padrão (Requerido pelo JPA) ---
    public Disciplina() {
    }

    // --- Getters e Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getProfessor() { return professor; }
    public void setProfessor(String professor) { this.professor = professor; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public int getCargaHoraria() { return cargaHoraria; }
    public void setCargaHoraria(int cargaHoraria) { this.cargaHoraria = cargaHoraria; }

    public Integer getQtdSemanas() { return qtdSemanas; }
    public void setQtdSemanas(Integer qtdSemanas) { this.qtdSemanas = qtdSemanas; }

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

    public boolean isAtiva() { return isAtiva; } // Getter para boolean is...
    
    public void setAtiva(boolean ativa) { this.isAtiva = ativa; }

    public boolean isReceberNotificacoes() { return receberNotificacoes; }
    public void setReceberNotificacoes(boolean receberNotificacoes) { this.receberNotificacoes = receberNotificacoes; }

    public Integer getAusenciasPermitidas() { return ausenciasPermitidas; }
    public void setAusenciasPermitidas(Integer ausenciasPermitidas) { this.ausenciasPermitidas = ausenciasPermitidas; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public List<Avaliacao> getAvaliacoes() { return avaliacoes; }
    public void setAvaliacoes(List<Avaliacao> avaliacoes) {
        // Limpar a coleção antiga e adicionar todos da nova, mantendo a consistência bidirecional
        if (this.avaliacoes != null) {
            for (Avaliacao aval : new ArrayList<>(this.avaliacoes)) { // Itera sobre uma cópia para evitar ConcurrentModificationException
                removeAvaliacao(aval); // Usa o método helper que cuida da bidirecionalidade
            }
        }
        if (avaliacoes != null) {
            for (Avaliacao aval : avaliacoes) {
                addAvaliacao(aval); // Usa o método helper que cuida da bidirecionalidade
            }
        }
    }

    // Métodos utilitários para gerenciar a coleção 'avaliacoes' (bidirecional)
    public void addAvaliacao(Avaliacao avaliacao) {
        if (avaliacao != null && !this.avaliacoes.contains(avaliacao)) {
            this.avaliacoes.add(avaliacao);
            avaliacao.setDisciplina(this); // Mantém a consistência bidirecional
        }
    }

    public void removeAvaliacao(Avaliacao avaliacao) {
        if (avaliacao != null && this.avaliacoes.contains(avaliacao)) {
            this.avaliacoes.remove(avaliacao);
            avaliacao.setDisciplina(null); // Mantém a consistência bidirecional
        }
    }

    public List<HorarioAula> getAulas() { return aulas; }
    public void setAulas(List<HorarioAula> aulas) {
        // Similar ao setAvaliacoes, para manter consistência se HorarioAula tiver back-reference
        if (this.aulas != null) {
            for (HorarioAula aula : new ArrayList<>(this.aulas)) {
                removeAula(aula);
            }
        }
        if (aulas != null) {
            for (HorarioAula aula : aulas) {
                addAula(aula);
            }
        }
    }

    // Métodos utilitários para gerenciar a coleção 'aulas' (bidirecional)
    public void addAula(HorarioAula aula) {
        if (aula != null && !this.aulas.contains(aula)) {
            this.aulas.add(aula);
            aula.setDisciplina(this); // Assume que HorarioAula tem setDisciplina(this)
        }
    }

    public void removeAula(HorarioAula aula) {
        if (aula != null && this.aulas.contains(aula)) {
            this.aulas.remove(aula);
            aula.setDisciplina(null); // Assume que HorarioAula tem setDisciplina(null)
        }
    }

    public List<Ausencia> getAusencias() { return ausencias; }

    public void setAusencias(List<Ausencia> ausencias) {
        if (this.ausencias != null) {
            for (Ausencia ausencia : new ArrayList<>(this.ausencias)) {
                removeAusencia(ausencia);
            }
        }
        if (ausencias != null) {
            for (Ausencia ausencia : ausencias) {
                addAusencia(ausencia);
            }
        }
    }

    public void addAusencia(Ausencia ausencia) {
        if (ausencia != null && !this.ausencias.contains(ausencia)) {
            this.ausencias.add(ausencia);
            ausencia.setDisciplina(this);
        }
    }

    public void removeAusencia(Ausencia ausencia) {
        if (ausencia != null && this.ausencias.contains(ausencia)) {
            this.ausencias.remove(ausencia);
            ausencia.setDisciplina(null);
        }
    }

    // --- equals, hashCode, toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Disciplina that = (Disciplina) o;
        return Objects.equals(id, that.id); // Compara apenas pelo ID se não for nulo
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Usa ID para hashCode; se ID for nulo, será o hash de null
    }

    @Override
    public String toString() {
        return "Disciplina{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", codigo='" + codigo + '\'' +
                ", professor='" + professor + '\'' +
                ", qtdSemanas=" + qtdSemanas +
                ", ausenciasPermitidas=" + ausenciasPermitidas +
                ", numeroDeAusencias=" + (ausencias != null ? ausencias.size() : 0) +
                ", usuarioId=" + (usuario != null ? usuario.getId() : "null") +
                ", numeroDeAvaliacoes=" + (avaliacoes != null ? avaliacoes.size() : 0) +
                ", numeroDeAulas=" + (aulas != null ? aulas.size() : 0) +
                '}';
    }
}
