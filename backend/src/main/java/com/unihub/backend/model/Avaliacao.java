package com.unihub.backend.model;

import com.unihub.backend.model.enums.EstadoAvaliacao;
import com.unihub.backend.model.enums.Prioridade;
import com.unihub.backend.model.enums.Modalidade;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import java.time.LocalDate; // Import necessário
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Entity
@Table(name = "avaliacoes") // É uma boa prática nomear tabelas no plural e com snake_case
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255) // Definir um tamanho para Strings é uma boa prática
    private String descricao;

    // Relacionamento com Disciplina (presumindo que Disciplina é uma entidade)
    // Se uma disciplina pode ter várias avaliações (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY) // LAZY para não carregar a disciplina desnecessariamente
    @JoinColumn(name = "disciplina_id") // Nome da coluna de chave estrangeira
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(
            value = { "avaliacoes", "aulas", "usuario", "hibernateLazyInitializer", "handler" },
            allowSetters = true
    )

    private Disciplina disciplina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonBackReference("usuario-avaliacoes")
    private Usuario usuario;

    @Column(name = "tipo_avaliacao", length = 100)
    private String tipoAvaliacao;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "data_entrega", nullable = true)
    private LocalDateTime dataEntrega;



    private Double nota; // Pode ser nulo

    private Double peso; // Pode ser nulo

    // Renomeado de 'membros' para 'integrantes' para consistência com o data class
    @ManyToMany(fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "avaliacao_integrantes_contato", // Nome da tabela de junção atualizado
            joinColumns = @JoinColumn(name = "avaliacao_id"),
            inverseJoinColumns = @JoinColumn(name = "contato_id")
    )
    private List<Contato> integrantes = new ArrayList<>();

    @Enumerated(EnumType.STRING) // Armazena o nome da constante do enum como String no banco
    @Column(nullable = false)    // Prioridade não pode ser nula
    private Prioridade prioridade;

    @Enumerated(EnumType.STRING) // Armazena o nome da constante do enum como String no banco
    @Column(name = "estado", nullable = false) // Estado não pode ser nulo
    private EstadoAvaliacao estado;

    @Enumerated(EnumType.STRING) // Armazena o nome da constante do enum como String no banco
    @Column(name = "modalidade", nullable = false) // Estado não pode ser nulo
    private Modalidade modalidade;

    private Integer dificuldade; // Pode ser nulo, Integer para objetos, int para primitivos
    private boolean receberNotificacoes;

    @Column(name = "google_calendar_event_id", length = 512)
    private String googleCalendarEventId;
    // --- Construtores ---

    /**
     * Construtor padrão requerido pelo JPA.
     */
    public Avaliacao() {
    }

    /**
     * Construtor com campos obrigatórios.
     * Considere adicionar outros campos conforme a necessidade da sua lógica de negócios.
     */
    public Avaliacao(
            String descricao, Disciplina disciplina, String tipoAvaliacao,
            Double nota, Double peso, List<Contato> integrantes,
            LocalDateTime dataEntrega, Prioridade prioridade, EstadoAvaliacao estado,
            Modalidade modalidade, Integer dificuldade, boolean receberNotificacoes) {
        this.dataEntrega = dataEntrega;
        this.prioridade = prioridade;
        this.estado = estado;
        this.modalidade = modalidade;
        this.descricao = descricao;
        this.disciplina = disciplina;
        this.tipoAvaliacao = tipoAvaliacao;
        this.nota = nota;
        this.peso = peso;
        this.integrantes = integrantes;
        this.dificuldade = dificuldade;
        this.receberNotificacoes = receberNotificacoes;
    }

    // --- Getters e Setters ---
    // (Gerados para todos os campos)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(Disciplina disciplina) {
        this.disciplina = disciplina;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTipoAvaliacao() {
        return tipoAvaliacao;
    }

    public void setTipoAvaliacao(String tipoAvaliacao) {
        this.tipoAvaliacao = tipoAvaliacao;
    }

    public LocalDateTime getDataEntrega() {
        return dataEntrega;
    }
    public void setDataEntrega(LocalDateTime dataEntrega) {
        this.dataEntrega = dataEntrega;
    }

    public Double getNota() {
        return nota;
    }

    public void setNota(Double nota) {
        this.nota = nota;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public List<Contato> getIntegrantes() {
        return integrantes;
    }

    // Não é comum ter um setter para uma coleção inteira.
    // É preferível usar métodos add/remove.
    // public void setIntegrantes(List<Contato> integrantes) {
    //    this.integrantes = integrantes;
    // }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
    }

    public Modalidade getModalidade() {
        return modalidade;
    }

    public void setModalidade(Modalidade modalidade) {
        this.modalidade = modalidade;
    }

    public String getGoogleCalendarEventId() {
        return googleCalendarEventId;
    }

    public void setGoogleCalendarEventId(String googleCalendarEventId) {
        this.googleCalendarEventId = googleCalendarEventId;
    }

    public EstadoAvaliacao getEstado() {
        return estado;
    }

    public void setEstado(EstadoAvaliacao estado) {
        this.estado = estado;
    }

    public Integer getDificuldade() {
        return dificuldade;
    }

    public void setDificuldade(Integer dificuldade) {
        this.dificuldade = dificuldade;
    }

    public boolean getReceberNotificacoes() {
        return receberNotificacoes;
    }

    public void setReceberNotificacoes(boolean receberNotificacoes) {
        this.receberNotificacoes = receberNotificacoes;
    }


    // --- Métodos utilitários para 'integrantes' ---

    public void addIntegrante(Contato contato) {
        if (contato != null && !this.integrantes.contains(contato)) {
            this.integrantes.add(contato);
            // Se o relacionamento for bidirecional e Contato tiver uma lista de Avaliacoes,
            // você também precisaria adicionar esta Avaliacao à lista do Contato:
            // contato.getAvaliacoes().add(this); // Exemplo
        }
    }

    public void removeIntegrante(Contato contato) {
        if (contato != null) {
            this.integrantes.remove(contato);
            // Se o relacionamento for bidirecional:
            // contato.getAvaliacoes().remove(this); // Exemplo
        }
    }

    public void clearIntegrantes() {
        // Se o relacionamento for bidirecional, iterar e remover de ambos os lados
        // for (Contato integrante : new ArrayList<>(this.integrantes)) {
        //    removeIntegrante(integrante);
        // }
        this.integrantes.clear();
    }

    // --- equals, hashCode, toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Avaliacao avaliacao = (Avaliacao) o;
        // Se o ID for nulo (entidade ainda não persistida),
        // pode ser necessário comparar outros campos únicos ou usar referências de objeto.
        // Mas para entidades gerenciadas, o ID é a melhor aposta após a persistência.
        return id != null && Objects.equals(id, avaliacao.id);
    }

    @Override
    public int hashCode() {
        // Se a entidade ainda não foi persistida, o ID será nulo.
        // O hashCode pode mudar após a persistência se baseado apenas no ID.
        // Para consistência, especialmente se usado em Sets antes da persistência,
        // pode-se retornar um valor fixo ou calcular com base em campos imutáveis/de negócio.
        // No entanto, para entidades JPA, é comum basear-se no ID.
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Avaliacao{" +
                "id=" + id +
                ", descricao='" + descricao + '\'' +
                ", disciplina=" + (disciplina != null ? disciplina.getId() : "null") + // Evitar NPe e mostrar ID
                ", tipoAvaliacao='" + tipoAvaliacao + '\'' +
                ", dataEntrega=" + dataEntrega +
                ", nota=" + nota +
                ", peso=" + peso +
                ", prioridade=" + prioridade +
                ", estado=" + estado +
                ", modalidade=" + modalidade +
                ", dificuldade=" + dificuldade +
                ", numeroDeIntegrantes=" + (integrantes != null ? integrantes.size() : 0) +
                '}';
    }
}
