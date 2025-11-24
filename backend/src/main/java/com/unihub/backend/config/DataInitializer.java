package com.unihub.backend.config;

import com.unihub.backend.model.*;
import com.unihub.backend.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.persistence.EntityNotFoundException;
import java.util.Objects;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import com.unihub.backend.model.enums.EstadoPlanejamento;
import com.unihub.backend.model.enums.Modalidade;
import com.unihub.backend.model.enums.Prioridade;
import com.unihub.backend.model.enums.QuadroStatus;
import com.unihub.backend.model.enums.TarefaStatus;
import com.unihub.backend.model.enums.EstadoAvaliacao;

@Component
public class DataInitializer {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private AusenciaRepository ausenciaRepository;

    @Autowired
    private ContatoRepository contatoRepository;

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private QuadroPlanejamentoRepository quadroPlanejamentoRepository;

    @Autowired
    private ColunaPlanejamentoRepository colunaPlanejamentoRepository;

    @Autowired
    private TarefaPlanejamentoRepository tarefaPlanejamentoRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private AnotacoesRepository anotacoesRepository;

    @Autowired
    private NotificacaoConfiguracaoRepository notificacaoConfiguracaoRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private TarefaComentarioRepository tarefaComentarioRepository;

    @Autowired
    private TarefaNotificacaoRepository tarefaNotificacaoRepository;

    @Autowired
    private ConviteCompartilhamentoRepository conviteCompartilhamentoRepository;

    @PostConstruct
    public void init() {
        initUsuarios();
        usuarioRepository.findByEmail("vanessa@email.com").ifPresent(this::initVanessa);
        usuarioRepository.findByEmail("victoria@email.com").ifPresent(this::initVictoria);
        usuarioRepository.findByEmail("rafaella@email.com").ifPresent(this::initRafaella);
        usuarioRepository.findByEmail("paulo@email.com").ifPresent(this::initPaulo);
    }

    private void initUsuarios() {
        criarUsuarioSeNaoExistir("Vanessa", "vanessa@email.com", "vanessa");
        criarUsuarioSeNaoExistir("Victoria", "victoria@email.com", "victoria");
        criarUsuarioSeNaoExistir("Rafaella", "rafaella@email.com", "rafaella");
        criarUsuarioSeNaoExistir("Paulo", "paulo@email.com", "pauloo");

        criarUsuarioSeNaoExistir("Carla Mendes", "carla.mendes@email.com", "carlamendes");
        criarUsuarioSeNaoExistir("Daniel Souza", "daniel.souza@email.com", "danielsouza");
        criarUsuarioSeNaoExistir("Elisa Ferreira", "elisa.ferreira@email.com", "elisaferreira");
        criarUsuarioSeNaoExistir("Felipe Oliveira", "felipe.oliveira@email.com", "felipeoliveira");
        criarUsuarioSeNaoExistir("Gabriela Santos", "gabriela.santos@email.com", "gabrielasantos");
        criarUsuarioSeNaoExistir("Heitor Lima", "heitor.lima@email.com", "heitorlima");
        criarUsuarioSeNaoExistir("Isabela Rocha", "isabela.rocha@email.com", "isabelarocha");
        criarUsuarioSeNaoExistir("João Pereira", "joao.pereira@email.com", "joaopereira");
        criarUsuarioSeNaoExistir("Karina Alves", "karina.alves@email.com", "karinaalves");
        criarUsuarioSeNaoExistir("Lucas Martins", "lucas.martins@email.com", "lucasmartins");
        criarUsuarioSeNaoExistir("Mariana Dias", "mariana.dias@email.com", "marianadias");
        criarUsuarioSeNaoExistir("Nicolas Teixeira", "nicolas.teixeira@email.com", "nicolasteixeira");
        criarUsuarioSeNaoExistir("Olívia Barbosa", "olivia.barbosa@email.com", "oliviabarbosa");
    }

    private void criarUsuarioSeNaoExistir(String nome, String email, String senha) {
        if (usuarioRepository.findByEmail(email).isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.setNomeUsuario(nome);
            usuario.setEmail(email);
            usuario.setSenha(passwordEncoder.encode(senha));
            usuarioRepository.save(usuario);
        }
    }

    private void initVanessa(Usuario usuario) {
        criarInstituicaoSeNaoExistir(usuario, "UFPR", 70.0, 75);

        criarCategoriaSeNaoExistir(usuario, "Trabalho");
        criarCategoriaSeNaoExistir(usuario, "Saúde");
        criarCategoriaSeNaoExistir(usuario, "Pessoal");

        Disciplina bd3 = criarDisciplinaSeNaoExistir(usuario,
                "DS340A", "Banco de Dados III", "João Marynowski",
                "20252", 30, 16, 4, LocalDate.of(2025, 8, 4), LocalDate.of(2025, 11, 26),
                "jeugenio@ufpr.br", "TEAMS e UFPR Virtual", "", "B0", true, true,
                new HorarioAula[]{criarHorarioAula("segunda-feira", "A13", 1140, 1360)});

        Disciplina saob22 = criarDisciplinaSeNaoExistir(usuario,
                "SAOB22", "Modelagem de Novos Negócios", "Cleverson Renan da Cunha",
                "20252", 60, 18, 4, LocalDate.of(2025, 8, 4), LocalDate.of(2025, 11, 26),
                "cleverson@ufpr.br", "TEAMS", "(41)98765-4321", "B0", false, true,
                new HorarioAula[]{criarHorarioAula("terça-feira", "210", 1140, 1360)});

        Disciplina planejamentoTributario = criarDisciplinaSeNaoExistir(usuario,
                "SA060", "Planejamento Tributário", "Clayton Gomes de Medeiros",
                "20252", 60, 18, 4, LocalDate.of(2025, 8, 4), LocalDate.of(2025, 11, 26),
                "clayton@ufpr.br", "WhatsApp", "", "B0", true, true,
                new HorarioAula[]{
                        criarHorarioAula("quarta-feira", "229", 1140, 1240),
                        criarHorarioAula("sexta-feira", "229", 1260, 1360)
                });

        Disciplina matematicaFinanceira = criarDisciplinaSeNaoExistir(usuario,
                "ST015", "Matemática Financeira", "Eloisa Rasotti Navarro",
                "20252", 30, 16, 4, LocalDate.of(2025, 8, 4), LocalDate.of(2025, 11, 26),
                "eloisa@ufpr.br", "UFPR Virtual", "", "B0", true, true,
                new HorarioAula[]{criarHorarioAula("quinta-feira", "a09", 1260, 1360)});

        if (bd3 != null) {
            Avaliacao av1 = new Avaliacao();
            av1.setUsuario(usuario);
            av1.setDisciplina(bd3);
            av1.setDescricao("Trabalho DW");
            av1.setTipoAvaliacao("Trabalho");
            av1.setPeso(40.0);
            av1.setNota(85.0);
            av1.setDataEntrega(LocalDate.of(2025, 11, 1).atTime(23, 59));
            av1.setPrioridade(Prioridade.MEDIA);
            av1.setEstado(EstadoAvaliacao.CONCLUIDA);
            av1.setModalidade(Modalidade.INDIVIDUAL);
            avaliacaoRepository.save(av1);

            Avaliacao av2 = new Avaliacao();
            av2.setUsuario(usuario);
            av2.setDisciplina(bd3);
            av2.setDescricao("Trabalho BD");
            av2.setTipoAvaliacao("Trabalho");
            av2.setPeso(30.0);
            av2.setNota(null);
            av2.setDataEntrega(LocalDate.of(2025, 11, 24).atTime(23, 59));
            av2.setPrioridade(Prioridade.MEDIA);
            av2.setEstado(EstadoAvaliacao.EM_ANDAMENTO);
            av2.setModalidade(Modalidade.INDIVIDUAL);
            avaliacaoRepository.save(av2);

            Avaliacao av3 = new Avaliacao();
            av3.setUsuario(usuario);
            av3.setDisciplina(bd3);
            av3.setDescricao("Tarefas");
            av3.setTipoAvaliacao("Atividades");
            av3.setPeso(30.0);
            av3.setNota(null);
            av3.setDataEntrega(LocalDate.of(2025, 12, 1).atTime(23, 59));
            av3.setPrioridade(Prioridade.BAIXA);
            av3.setEstado(EstadoAvaliacao.EM_ANDAMENTO);
            av3.setModalidade(Modalidade.INDIVIDUAL);
            avaliacaoRepository.save(av3);

            Anotacoes n1 = new Anotacoes();
            n1.setTitulo("Aula 1");
            n1.setConteudo("Introdução a bancos de dados e arquitetura relacional.");
            n1.setDisciplina(bd3);
            anotacoesRepository.save(n1);

            Anotacoes n2 = new Anotacoes();
            n2.setTitulo("Aula 2");
            n2.setConteudo("Modelo entidade-relacionamento e cardinalidades.");
            n2.setDisciplina(bd3);
            anotacoesRepository.save(n2);

            criarAusenciaSeNaoExistir(usuario, bd3, LocalDate.of(2025, 11, 10),
                    "Falta em BD3", "Pessoal");
            criarAusenciaSeNaoExistir(usuario, bd3, LocalDate.of(2025, 11, 17),
                    "Falta em BD3", "Pessoal");
        }

        if (planejamentoTributario != null) {
            criarAusenciaSeNaoExistir(usuario, planejamentoTributario, LocalDate.of(2025, 11, 19),
                    "Falta em Planejamento Tributário", "Pessoal");
        }

        criarContatoSeNaoExistir(usuario, "Rafaella", "rafaella@email.com", false);
    

        criarContatoSeNaoExistir(usuario, "Carla Mendes", "carla.mendes@email.com", false);
        criarContatoSeNaoExistir(usuario, "Daniel Souza", "daniel.souza@email.com", false);
        criarContatoSeNaoExistir(usuario, "Elisa Ferreira", "elisa.ferreira@email.com", false);

        criarContatoSeNaoExistir(usuario, "Felipe Oliveira", "felipe.oliveira@email.com", true);
        criarContatoSeNaoExistir(usuario, "Gabriela Santos", "gabriela.santos@email.com", true);
        criarContatoSeNaoExistir(usuario, "Heitor Lima", "heitor.lima@email.com", true);

        seedPlanejamentosVanessa(usuario, bd3, planejamentoTributario);

        seedConfiguracoesComunicacao(usuario);
        seedAvaliacaoEAnotacao(usuario);
    }

    private void initVictoria(Usuario usuario) {
        criarInstituicaoSeNaoExistir(usuario, "UFPR", 70.0, 75);
        criarCategoriaSeNaoExistir(usuario, "Trabalho");
        criarCategoriaSeNaoExistir(usuario, "Saúde");
        criarCategoriaSeNaoExistir(usuario, "Pessoal");
        seedTadsDisciplinas(usuario);

        seedConfiguracoesComunicacao(usuario);
        seedAvaliacaoEAnotacao(usuario);
    }

    private List<Disciplina> seedTadsDisciplinas(Usuario usuario) {
        Disciplina bancoDadosI = criarDisciplinaSeNaoExistir(usuario,
                "DS320", "Banco de Dados I", "Jeroniza Marchaukoski",
                "20252", 60, 18, 5, LocalDate.of(2025, 8, 4), LocalDate.of(2025, 11, 26),
                "jeroniza.marchaukoski@ufpr.br", "Presencial", "", "A15 Lab", true, true,
                new HorarioAula[]{
                        criarHorarioAula("segunda-feira", "A15 Lab", 1140, 1200)
                });

        Disciplina desenvolvimentoWebI = criarDisciplinaSeNaoExistir(usuario,
                "DS122", "Desenvolvimento Web I", "Alexander Kutzke",
                "20252", 60, 18, 5, LocalDate.of(2025, 8, 4), LocalDate.of(2025, 11, 26),
                "alexander.kutzke@ufpr.br", "Presencial", "", "A15 Lab", true, true,
                new HorarioAula[]{
                        criarHorarioAula("segunda-feira", "A15 Lab", 1260, 1320)
                });

        Disciplina analiseProjetoSistemasI = criarDisciplinaSeNaoExistir(usuario,
                "DS220", "Análise e Projeto de Sistemas I", "Jaime Wojciechowski",
                "20252", 60, 18, 5, LocalDate.of(2025, 8, 4), LocalDate.of(2025, 11, 26),
                "jaime.wojciechowski@ufpr.br", "Presencial", "", "A08", false, true,
                new HorarioAula[]{
                        criarHorarioAula("terça-feira", "A08", 1140, 1200)
                });

        return Arrays.asList(bancoDadosI, desenvolvimentoWebI, analiseProjetoSistemasI);
    }

    private void seedPlanejamentosVanessa(Usuario usuario, Disciplina bd3, Disciplina planejamentoTributario) {

        if (bd3 != null) {
            LocalDateTime prazoBd3 = bd3.getDataFimSemestre().atTime(23, 59);

            QuadroPlanejamento quadroBd3 = criarQuadro(
                    usuario,
                    bd3.getCodigo() + " - " + bd3.getNome(),
                    QuadroStatus.ATIVO,
                    prazoBd3
            );

            if (colunaPlanejamentoRepository.findByQuadroIdOrderByOrdemAsc(quadroBd3.getId()).isEmpty()) {
                ColunaPlanejamento colunaDatawarehouse = criarColuna(
                        quadroBd3,
                        "Trabalho Datawerehouse",
                        EstadoPlanejamento.CONCLUIDO,
                        1
                );

                ColunaPlanejamento colunaBigData = criarColuna(
                        quadroBd3,
                        "Trabalho Big Data",
                        EstadoPlanejamento.EM_ANDAMENTO,
                        2
                );

                LocalDate base = bd3.getDataFimSemestre();

                criarTarefa(
                        colunaDatawarehouse,
                        "Entrega do artigo",
                        "Entrega do artigo de Datawarehouse",
                        TarefaStatus.CONCLUIDA,
                        base.minusDays(7)
                );
                criarTarefa(
                        colunaDatawarehouse,
                        "Defesa",
                        "Defesa do trabalho de Datawarehouse",
                        TarefaStatus.CONCLUIDA,
                        base.minusDays(3)
                );

                criarTarefa(
                        colunaBigData,
                        "Entrega do artigo",
                        "Entrega do artigo de Big Data",
                        TarefaStatus.CONCLUIDA,
                        base.minusDays(2)
                );
                criarTarefa(
                        colunaBigData,
                        "Defesa",
                        "Defesa do trabalho de Big Data",
                        TarefaStatus.PENDENTE,
                        base
                );
            }
        }

        if (planejamentoTributario != null) {
            LocalDateTime prazoTrib = planejamentoTributario.getDataFimSemestre().atTime(23, 59);

            QuadroPlanejamento quadroTrib = criarQuadro(
                    usuario,
                    planejamentoTributario.getCodigo() + " - " + planejamentoTributario.getNome(),
                    QuadroStatus.ATIVO,
                    prazoTrib
            );

            if (colunaPlanejamentoRepository.findByQuadroIdOrderByOrdemAsc(quadroTrib.getId()).isEmpty()) {
                ColunaPlanejamento colunaTarefas = criarColuna(
                        quadroTrib,
                        "Tarefas",
                        EstadoPlanejamento.EM_ANDAMENTO,
                        1
                );

                criarTarefa(
                        colunaTarefas,
                        "Tarefa 15/10",
                        "Atividade de 15/10",
                        TarefaStatus.CONCLUIDA,
                        LocalDate.of(2025, 10, 15)
                );
                criarTarefa(
                        colunaTarefas,
                        "Tarefa 22/10",
                        "Atividade de 22/10",
                        TarefaStatus.CONCLUIDA,
                        LocalDate.of(2025, 10, 22)
                );
                criarTarefa(
                        colunaTarefas,
                        "Tarefa 12/11",
                        "Atividade de 12/11",
                        TarefaStatus.PENDENTE,
                        LocalDate.of(2025, 11, 12)
                );
                criarTarefa(
                        colunaTarefas,
                        "Tarefa 26/11",
                        "Atividade de 26/11",
                        TarefaStatus.PENDENTE,
                        LocalDate.of(2025, 11, 26)
                );
            }
        }
    }

    private void seedConfiguracoesComunicacao(Usuario usuario) {
        boolean possuiConfig = notificacaoConfiguracaoRepository.findAll().stream()
                .anyMatch(cfg -> cfg.getUsuario().getId().equals(usuario.getId()));
        if (!possuiConfig) {
            NotificacaoConfiguracao config = new NotificacaoConfiguracao();
            config.setUsuario(usuario);
            config.setComentarioTarefa(true);
            config.setInclusoEmGrupo(true);

            NotificacaoConfiguracaoAntecedencia alta = new NotificacaoConfiguracaoAntecedencia();
            alta.setPrioridade(Prioridade.ALTA);
            alta.setAntecedencia(com.unihub.backend.model.enums.Antecedencia.UM_DIA);
            config.addAntecedencia(alta);

            notificacaoConfiguracaoRepository.save(config);
        }
    }

    private void seedAvaliacaoEAnotacao(Usuario usuario) {
        List<Disciplina> disciplinas = disciplinaRepository.findByUsuarioId(usuario.getId());
        if (disciplinas.isEmpty()) {
            return;
        }
        Disciplina disciplina = disciplinas.get(0);

        if (anotacoesRepository.findAll().stream().noneMatch(a -> a.getDisciplina().getId().equals(disciplina.getId()))) {
            Anotacoes anotacao = new Anotacoes();
            anotacao.setTitulo("Pontos-chave");
            anotacao.setConteudo("Resumo inicial para manter a tabela preenchida.");
            anotacao.setDisciplina(disciplina);
            anotacoesRepository.save(anotacao);
        }

        if (avaliacaoRepository.findByUsuarioId(usuario.getId()).isEmpty()) {
            Avaliacao avaliacao = new Avaliacao();
            avaliacao.setUsuario(usuario);
            avaliacao.setDisciplina(disciplina);
            avaliacao.setDescricao("P1");
            avaliacao.setTipoAvaliacao("Prova");

            LocalDate inicio = disciplina.getDataInicioSemestre();
            LocalDateTime dataEntrega;
            if (inicio != null) {
                dataEntrega = inicio.plusWeeks(4).atTime(23, 59);
            } else {
                dataEntrega = LocalDateTime.of(2025, 9, 1, 23, 59);
            }
            avaliacao.setDataEntrega(dataEntrega);

            avaliacao.setPrioridade(Prioridade.ALTA);
            avaliacao.setEstado(EstadoAvaliacao.EM_ANDAMENTO);
            avaliacao.setModalidade(Modalidade.INDIVIDUAL);
            avaliacao.setDificuldade(3);
            avaliacao.setReceberNotificacoes(true);
            avaliacaoRepository.save(avaliacao);
        }
    }

    private QuadroPlanejamento criarQuadro(Usuario usuario, String titulo, QuadroStatus status, LocalDateTime prazo) {
        return quadroPlanejamentoRepository.findByUsuarioId(usuario.getId()).stream()
                .filter(q -> q.getTitulo().equalsIgnoreCase(titulo))
                .findFirst()
                .orElseGet(() -> {
                    QuadroPlanejamento quadro = new QuadroPlanejamento();
                    quadro.setUsuario(usuario);
                    quadro.setTitulo(titulo);
                    quadro.setStatus(status);
                    quadro.setDataPrazo(prazo);
                    return quadroPlanejamentoRepository.save(quadro);
                });
    }

    private ColunaPlanejamento criarColuna(QuadroPlanejamento quadro, String titulo, EstadoPlanejamento estado, int ordem) {
        ColunaPlanejamento coluna = new ColunaPlanejamento();
        coluna.setQuadro(quadro);
        coluna.setTitulo(titulo);
        coluna.setEstado(estado);
        coluna.setOrdem(ordem);
        return colunaPlanejamentoRepository.save(coluna);
    }

    private TarefaPlanejamento criarTarefa(ColunaPlanejamento coluna,
                                           String titulo,
                                           String descricao,
                                           TarefaStatus status,
                                           LocalDate dataPrazo) {
        TarefaPlanejamento tarefa = new TarefaPlanejamento();
        tarefa.setColuna(coluna);
        tarefa.setTitulo(titulo);
        tarefa.setDescricao(descricao);
        tarefa.setStatus(status);
        if (dataPrazo != null) {
            tarefa.setDataPrazo(dataPrazo.atTime(23, 59));
        }
        return tarefaPlanejamentoRepository.save(tarefa);
    }

    private void initRafaella(Usuario usuario) {
        criarInstituicaoSeNaoExistir(usuario, "UFPR", 70.0, 75);
        criarCategoriaSeNaoExistir(usuario, "Trabalho");
        criarCategoriaSeNaoExistir(usuario, "Saúde");
        criarCategoriaSeNaoExistir(usuario, "Pessoal");
        seedTadsDisciplinas(usuario);

        seedConfiguracoesComunicacao(usuario);
        seedAvaliacaoEAnotacao(usuario);
    }

    private void initPaulo(Usuario usuario) {
        criarInstituicaoSeNaoExistir(usuario, "UFPR", 70.0, 75);

        criarCategoriaSeNaoExistir(usuario, "Trabalho");
        criarCategoriaSeNaoExistir(usuario, "Saúde");
        criarCategoriaSeNaoExistir(usuario, "Pessoal");

        Contato anaSilva = criarContatoSeNaoExistir(usuario, "Ana Silva-pend", "ana.silva@email.com",  true);
        Contato brunoCosta = criarContatoSeNaoExistir(usuario, "Bruno Costa-pend", "bruno.costa@email.com",  true);
        Contato carlaMendes = criarContatoSeNaoExistir(usuario, "Carla Mendes-pend", "carla.mendes@email.com",  true);
        Contato danielSouza = criarContatoSeNaoExistir(usuario, "Daniel Souza-pend", "daniel.souza@email.com",  true);
        Contato elisaFerreira = criarContatoSeNaoExistir(usuario, "Elisa Ferreira-pend", "elisa.ferreira@email.com",  true);
        Contato felipeOliveira = criarContatoSeNaoExistir(usuario, "Felipe Oliveira-pend", "felipe.oliveira@email.com",  true);
        Contato gabrielaSantos = criarContatoSeNaoExistir(usuario, "Gabriela Santos", "gabriela.santos@email.com", false);
        Contato heitorLima = criarContatoSeNaoExistir(usuario, "Heitor Lima", "heitor.lima@email.com", false);
        Contato isabelaRocha = criarContatoSeNaoExistir(usuario, "Isabela Rocha", "isabela.rocha@email.com",  false);
        Contato joaoPereira = criarContatoSeNaoExistir(usuario, "João Pereira", "joao.pereira@email.com",  false);
        Contato karinaAlves = criarContatoSeNaoExistir(usuario, "Karina Alves", "karina.alves@email.com",  false);
        Contato lucasMartins = criarContatoSeNaoExistir(usuario, "Lucas Martins", "lucas.martins@email.com",  false);
        Contato marianaDias = criarContatoSeNaoExistir(usuario, "Mariana Dias", "mariana.dias@email.com",  false);
        Contato nicolasTeixeira = criarContatoSeNaoExistir(usuario, "Nicolas Teixeira", "nicolas.teixeira@email.com",  false);
        Contato oliviaBarbosa = criarContatoSeNaoExistir(usuario, "Olívia Barbosa", "olivia.barbosa@email.com",  false);

        seedTadsDisciplinas(usuario);
        
        seedConfiguracoesComunicacao(usuario);
        seedAvaliacaoEAnotacao(usuario);
    }

    private void criarInstituicaoSeNaoExistir(Usuario usuario, String nome, double media, int freq) {
        boolean exists = instituicaoRepository.findByUsuarioIdOrderByNomeAsc(usuario.getId()).stream()
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
                                                   String professor, String periodo, int cargaHoraria, Integer qtdSemanas,
                                                   Integer ausenciasPesmitidas,
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
        disciplina.setQtdSemanas(qtdSemanas);
        disciplina.setAusenciasPermitidas(ausenciasPesmitidas);
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

    private Contato criarContatoSeNaoExistir(Usuario usuario, String nome, String email, Boolean pendente){
        Contato contato = contatoRepository.findByOwnerId(usuario.getId()).stream()
                .filter(c -> (email != null && email.equalsIgnoreCase(c.getEmail()))
                        || c.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .orElseGet(() -> {
                    Contato novoContato = new Contato();
                    novoContato.setNome(nome);
                    novoContato.setEmail(email);
                    novoContato.setOwnerId(usuario.getId());
                    novoContato.setPendente(pendente);
                    novoContato.setIdContato(null);

                    LocalDateTime dataPadrao = LocalDateTime.of(2025, 8, 10, 18, 0);
                    novoContato.setDataSolicitacao(dataPadrao);
                    novoContato.setDataConfirmacao(pendente ? null : dataPadrao);

                    return contatoRepository.save(novoContato);
                });

        if (!Boolean.TRUE.equals(contato.getPendente()) && email != null) {
            usuarioRepository.findByEmail(email)
                    .filter(u -> !u.getId().equals(usuario.getId()))
                    .ifPresent(usuarioContato -> {
                        boolean atualizado = false;
                        if (contato.getIdContato() == null || !contato.getIdContato().equals(usuarioContato.getId())) {
                            contato.setIdContato(usuarioContato.getId());
                            atualizado = true;
                        }
                        if (contato.getDataConfirmacao() == null) {
                            contato.setDataConfirmacao(LocalDateTime.of(2025, 8, 10, 18, 0));
                            atualizado = true;
                        }
                        if (atualizado) {
                            contatoRepository.save(contato);
                        }
                        criarContatoReciprocoSeNecessario(usuario, usuarioContato);
                    });
        }

        return contato;
    }

    private void criarContatoReciprocoSeNecessario(Usuario usuarioOrigem, Usuario usuarioContato) {
        if (usuarioOrigem.getId().equals(usuarioContato.getId())) {
            return;
        }

        Contato reciproco = contatoRepository.findByOwnerId(usuarioContato.getId()).stream()
                .filter(c -> {
                    if (c.getIdContato() != null) {
                        return c.getIdContato().equals(usuarioOrigem.getId());
                    }
                    return c.getEmail() != null && c.getEmail().equalsIgnoreCase(usuarioOrigem.getEmail());
                })
                .findFirst()
                .orElse(null);

        LocalDateTime dataPadrao = LocalDateTime.of(2025, 8, 10, 18, 0);

        if (reciproco == null) {
            Contato novoContato = new Contato();
            novoContato.setOwnerId(usuarioContato.getId());
            novoContato.setNome(usuarioOrigem.getNomeUsuario());
            novoContato.setEmail(usuarioOrigem.getEmail());
            novoContato.setPendente(false);
            novoContato.setIdContato(usuarioOrigem.getId());
            novoContato.setDataSolicitacao(dataPadrao);
            novoContato.setDataConfirmacao(dataPadrao);
            contatoRepository.save(novoContato);
            return;
        }

        boolean atualizado = false;
        if (!Boolean.FALSE.equals(reciproco.getPendente())) {
            reciproco.setPendente(false);
            atualizado = true;
        }
        if (reciproco.getIdContato() == null || !reciproco.getIdContato().equals(usuarioOrigem.getId())) {
            reciproco.setIdContato(usuarioOrigem.getId());
            atualizado = true;
        }
        if (usuarioOrigem.getNomeUsuario() != null && !usuarioOrigem.getNomeUsuario().equals(reciproco.getNome())) {
            reciproco.setNome(usuarioOrigem.getNomeUsuario());
            atualizado = true;
        }
        if (usuarioOrigem.getEmail() != null && (reciproco.getEmail() == null || !reciproco.getEmail().equalsIgnoreCase(usuarioOrigem.getEmail()))) {
            reciproco.setEmail(usuarioOrigem.getEmail());
            atualizado = true;
        }
        if (reciproco.getDataConfirmacao() == null) {
            reciproco.setDataConfirmacao(dataPadrao);
            atualizado = true;
        }
        if (reciproco.getDataSolicitacao() == null) {
            reciproco.setDataSolicitacao(dataPadrao);
            atualizado = true;
        }

        if (atualizado) {
            contatoRepository.save(reciproco);
        }
    }

    private Grupo criarGrupoSeNaoExistir(Usuario usuario, String nome, List<Contato> membros){
        return grupoRepository.findByOwnerId(usuario.getId()).stream()
                .filter(g -> g.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .orElseGet(() -> {
                    Grupo grupo = new Grupo();
                    grupo.setNome(nome);
                    grupo.setOwnerId(usuario.getId());
                    if (membros != null) {
                        membros.stream()
                                .filter(Objects::nonNull)
                                .map(Contato::getId)
                                .filter(Objects::nonNull)
                                .distinct()
                                .forEach(id -> adicionarMembroPersistido(grupo, id));
                    }
                    return grupoRepository.save(grupo);
                });
    }

    private void adicionarMembroPersistido(Grupo grupo, Long contatoId) {
        try {
            grupo.addMembro(contatoRepository.getReferenceById(contatoId));
        } catch (EntityNotFoundException ignored) {

        }
    }
}