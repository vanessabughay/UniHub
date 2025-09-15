package com.unihub.backend.config;

import com.unihub.backend.model.*;
import com.unihub.backend.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
@DependsOn("usuario")
public class DataInitializer {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private AusenciaRepository ausenciaRepository;

    @PostConstruct
    public void init() {
        usuarioRepository.findByEmail("vanessa@email.com").ifPresent(this::initVanessa);
        usuarioRepository.findByEmail("victoria@email.com").ifPresent(this::initVictoria);
        usuarioRepository.findByEmail("rafaella@email.com").ifPresent(this::initRafaella);
        usuarioRepository.findByEmail("paulo@email.com").ifPresent(this::initPaulo);
    }

    private void initVanessa(Usuario usuario) {
        criarInstituicaoSeNaoExistir(usuario, "UFPR", 70.0, 75);

        criarCategoriaSeNaoExistir(usuario, "Trabalho");
        criarCategoriaSeNaoExistir(usuario, "Saúde");
        criarCategoriaSeNaoExistir(usuario, "Pessoal");

        criarDisciplinaSeNaoExistir(usuario,
                "DS340A", "Banco de Dados III", "João Marynowski",
                "2025/2", 30, LocalDate.of(2025,8,4), LocalDate.of(2025,11,26),
                "jeugenio@ufpr.br", "TEAMS e UFPR Virtual", "(41)99999-9999", "B0", true, true,
                new HorarioAula[]{criarHorarioAula("segunda-feira", "A13", 1140, 2240)});

        Disciplina saob22 = criarDisciplinaSeNaoExistir(usuario,
                "SAOB22", "Modelagem de Novos Negócios", "Cleverson Renan da Cunha",
                "2025/2", 60, LocalDate.of(2025,8,4), LocalDate.of(2025,11,26),
                "cleverson@ufpr.br", "TEAMS", "(41)99999-9999", "B0", true, true,
                new HorarioAula[]{criarHorarioAula("terça-feira", "210", 1140, 1360)});

        criarDisciplinaSeNaoExistir(usuario,
                "SA060", "Planejamento Tributário", "Clayton Gomes de Medeiros",
                "2025/2", 60, LocalDate.of(2025,8,4), LocalDate.of(2025,11,26),
                "clayton@ufpr.br", "WhatsApp", "(41)99999-9999", "B0", true, true,
                new HorarioAula[]{criarHorarioAula("quarta-feira", "229", 1140, 1360)});

        criarDisciplinaSeNaoExistir(usuario,
                "ST015", "Matemática Financeira", "Eloisa Rasotti Navarro",
                "2025/2", 30, LocalDate.of(2025,8,4), LocalDate.of(2025,11,26),
                "eloisa@ufpr.br", "UFPR Virtual", "(41)99999-9999", "B0", true, true,
                new HorarioAula[]{criarHorarioAula("quinta-feira", "a09", 1260, 1360)});

        if (saob22 != null) {
            criarAusenciaSeNaoExistir(usuario, saob22, LocalDate.of(2025,8,19), "Congresso", "Trabalho");
            criarAusenciaSeNaoExistir(usuario, saob22, LocalDate.of(2025,9,9), "Show", "Pessoal");
        }
    }

    private void initVictoria(Usuario usuario) {
        criarInstituicaoSeNaoExistir(usuario, "UFPR", 70.0, 75);
        criarCategoriaSeNaoExistir(usuario, "Trabalho");
        criarCategoriaSeNaoExistir(usuario, "Saúde");
        criarCategoriaSeNaoExistir(usuario, "Pessoal");
    }

    private void initRafaella(Usuario usuario) {
        criarInstituicaoSeNaoExistir(usuario, "USP", 50.0, 75);
        criarCategoriaSeNaoExistir(usuario, "Trabalho");
        criarCategoriaSeNaoExistir(usuario, "Saúde");
        criarCategoriaSeNaoExistir(usuario, "Pessoal");
    }

    private void initPaulo(Usuario usuario) {
        criarInstituicaoSeNaoExistir(usuario, "UFPR", 80.0, 75);
    }

    private void criarInstituicaoSeNaoExistir(Usuario usuario, String nome, double media, int freq) {
        boolean exists = instituicaoRepository.findByUsuarioId(usuario.getId()).stream()
                .anyMatch(i -> i.getNome().equalsIgnoreCase(nome));
        if (!exists) {
            Instituicao inst = new Instituicao();
            inst.setNome(nome);
            inst.setMediaAprovacao(media);
            inst.setFrequenciaMinima(freq);
            inst.setUsuario(usuario);
            instituicaoRepository.save(inst);
        }
    }

    private void criarCategoriaSeNaoExistir(Usuario usuario, String nome) {
        if (categoriaRepository.findByNomeAndUsuarioId(nome, usuario.getId()).isEmpty()) {
            Categoria categoria = new Categoria();
            categoria.setNome(nome);
            categoria.setUsuario(usuario);
            categoriaRepository.save(categoria);
        }
    }

    private Disciplina criarDisciplinaSeNaoExistir(Usuario usuario, String codigo, String nome,
                                                  String professor, String periodo, int cargaHoraria,
                                                  LocalDate dataInicio, LocalDate dataFim,
                                                  String emailProfessor, String plataforma,
                                                  String telefoneProfessor, String salaProfessor,
                                                  boolean ativa, boolean receberNotificacoes,
                                                  HorarioAula[] aulas) {
        Disciplina existente = disciplinaRepository.findByUsuarioId(usuario.getId()).stream()
                .filter(d -> codigo.equalsIgnoreCase(d.getCodigo()))
                .findFirst()
                .orElse(null);
        if (existente != null) {
            return existente;
        }

        Disciplina disciplina = new Disciplina();
        disciplina.setCodigo(codigo);
        disciplina.setNome(nome);
        disciplina.setProfessor(professor);
        disciplina.setPeriodo(periodo);
        disciplina.setCargaHoraria(cargaHoraria);
        disciplina.setDataInicioSemestre(dataInicio);
        disciplina.setDataFimSemestre(dataFim);
        disciplina.setEmailProfessor(emailProfessor);
        disciplina.setPlataforma(plataforma);
        disciplina.setTelefoneProfessor(telefoneProfessor);
        disciplina.setSalaProfessor(salaProfessor);
        disciplina.setAtiva(ativa);
        disciplina.setReceberNotificacoes(receberNotificacoes);
        disciplina.setUsuario(usuario);
        List<HorarioAula> aulasList = Arrays.asList(aulas);
        disciplina.setAulas(aulasList);
        return disciplinaRepository.save(disciplina);
    }

    private HorarioAula criarHorarioAula(String dia, String sala, int inicio, int fim) {
        HorarioAula horario = new HorarioAula();
        horario.setDiaDaSemana(dia);
        horario.setSala(sala);
        horario.setHorarioInicio(inicio);
        horario.setHorarioFim(fim);
        return horario;
    }

    private void criarAusenciaSeNaoExistir(Usuario usuario, Disciplina disciplina, LocalDate data,
                                           String justificativa, String categoria) {
        boolean exists = ausenciaRepository.findByUsuarioId(usuario.getId()).stream()
                .anyMatch(a -> a.getDisciplina().getId().equals(disciplina.getId())
                        && a.getData().equals(data));
        if (!exists) {
            Ausencia ausencia = new Ausencia();
            ausencia.setUsuario(usuario);
            ausencia.setDisciplina(disciplina);
            ausencia.setData(data);
            ausencia.setJustificativa(justificativa);
            ausencia.setCategoria(categoria);
            ausenciaRepository.save(ausencia);
        }
    }
}