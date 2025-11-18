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

        criarUsuarioSeNaoExistir("A","a@a.com","000000");
        criarUsuarioSeNaoExistir("B","b@b.com","000000");
        criarUsuarioSeNaoExistir("C","c@c.com","000000");
        criarUsuarioSeNaoExistir("D","d@d.com","000000");



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

        criarDisciplinaSeNaoExistir(usuario,
                "DS340A", "Banco de Dados III", "João Marynowski",
                "2025/2", 30, 16,4, LocalDate.of(2025,8,4), LocalDate.of(2025,11,26),
                "jeugenio@ufpr.br", "TEAMS e UFPR Virtual", "(41)99999-9999", "B0", true, true,
                new HorarioAula[]{criarHorarioAula("segunda-feira", "A13", 1140, 1360)});

        Disciplina saob22 = criarDisciplinaSeNaoExistir(usuario,
                "SAOB22", "Modelagem de Novos Negócios", "Cleverson Renan da Cunha",
                "2025/2", 60, 18,4, LocalDate.of(2025,8,4), LocalDate.of(2025,11,26),
                "cleverson@ufpr.br", "TEAMS", "(41)99999-9999", "B0", true, true,
                new HorarioAula[]{criarHorarioAula("terça-feira", "210", 1140, 1360)});

        criarDisciplinaSeNaoExistir(usuario,
                "SA060", "Planejamento Tributário", "Clayton Gomes de Medeiros",
                "2025/2", 60, 18,4, LocalDate.of(2025,8,4), LocalDate.of(2025,11,26),
                "clayton@ufpr.br", "WhatsApp", "(41)99999-9999", "B0", true, true,
                new HorarioAula[]{criarHorarioAula("quarta-feira", "229", 1140, 1360)});

        criarDisciplinaSeNaoExistir(usuario,
                "ST015", "Matemática Financeira", "Eloisa Rasotti Navarro",
                "2025/2", 30, 16,4, LocalDate.of(2025,8,4), LocalDate.of(2025,11,26),
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
        criarInstituicaoSeNaoExistir(usuario, "UFPR", 70.0, 75);

        criarCategoriaSeNaoExistir(usuario, "Trabalho");
        criarCategoriaSeNaoExistir(usuario, "Saúde");
        criarCategoriaSeNaoExistir(usuario, "Pessoal");

        criarDisciplinaSeNaoExistir(usuario,
                "DS340A", "Disciplina I", "João Marynowski",
                "2025/2", 30, 16,4, LocalDate.of(2025,8,4), LocalDate.of(2025,11,26),
                "jeugenio@ufpr.br", "TEAMS e UFPR Virtual", "(41)99999-9999", "B0", true, true,
                new HorarioAula[]{criarHorarioAula("segunda-feira", "A13", 1140, 2240)});

        Disciplina saob11 = criarDisciplinaSeNaoExistir(usuario,
                "SAOB22", "Disciplina II", "Cleverson Renan da Cunha",
                "2025/2", 60, 18,4, LocalDate.of(2025,8,4), LocalDate.of(2025,11,26),
                "cleverson@ufpr.br", "TEAMS", "(41)99999-9999", "B0", true, true,
                new HorarioAula[]{criarHorarioAula("terça-feira", "210", 1140, 1360)});

        criarDisciplinaSeNaoExistir(usuario,
                "SA060", "Disciplina III", "Clayton Gomes de Medeiros",
                "2025/2", 60, 18,4, LocalDate.of(2025,8,4), LocalDate.of(2025,11,26),
                "clayton@ufpr.br", "WhatsApp", "(41)99999-9999", "B0", true, true,
                new HorarioAula[]{criarHorarioAula("quarta-feira", "229", 1140, 1360)});


        Contato anaSilva = criarContatoSeNaoExistir(usuario, "Ana Silva-pend", "ana.silva@email.com", "(41)90000-0001", true);
        Contato brunoCosta = criarContatoSeNaoExistir(usuario, "Bruno Costa-pend", "bruno.costa@email.com", "(41)90000-0002", true);
        Contato carlaMendes = criarContatoSeNaoExistir(usuario, "Carla Mendes-pend", "carla.mendes@email.com", "(41)90000-0003", true);
        Contato danielSouza = criarContatoSeNaoExistir(usuario, "Daniel Souza-pend", "daniel.souza@email.com", "(41)90000-0004", true);
        Contato elisaFerreira = criarContatoSeNaoExistir(usuario, "Elisa Ferreira-pend", "elisa.ferreira@email.com", "(41)90000-0005", true);
        Contato felipeOliveira = criarContatoSeNaoExistir(usuario, "Felipe Oliveira-pend", "felipe.oliveira@email.com", "(41)90000-0006", true);
        Contato gabrielaSantos = criarContatoSeNaoExistir(usuario, "Gabriela Santos", "gabriela.santos@email.com", "(41)90000-0007", false);
        Contato heitorLima = criarContatoSeNaoExistir(usuario, "Heitor Lima", "heitor.lima@email.com", "(41)90000-0008", false);
        Contato isabelaRocha = criarContatoSeNaoExistir(usuario, "Isabela Rocha", "isabela.rocha@email.com", "(41)90000-0009", false);
        Contato joaoPereira = criarContatoSeNaoExistir(usuario, "João Pereira", "joao.pereira@email.com", "(41)90000-0010", false);
        Contato karinaAlves = criarContatoSeNaoExistir(usuario, "Karina Alves", "karina.alves@email.com", "(41)90000-0011", false);
        Contato lucasMartins = criarContatoSeNaoExistir(usuario, "Lucas Martins", "lucas.martins@email.com", "(41)90000-0012", false);
        Contato marianaDias = criarContatoSeNaoExistir(usuario, "Mariana Dias", "mariana.dias@email.com", "(41)90000-0013", false);
        Contato nicolasTeixeira = criarContatoSeNaoExistir(usuario, "Nicolas Teixeira", "nicolas.teixeira@email.com", "(41)90000-0014", false);
        Contato oliviaBarbosa = criarContatoSeNaoExistir(usuario, "Olívia Barbosa", "olivia.barbosa@email.com", "(41)90000-0015", false);

        /*
        criarGrupoSeNaoExistir(usuario, "Família", List.of(anaSilva, brunoCosta, carlaMendes, danielSouza, elisaFerreira));
        criarGrupoSeNaoExistir(usuario, "Equipe de Projeto", List.of(felipeOliveira, gabrielaSantos, heitorLima,
                isabelaRocha, joaoPereira, karinaAlves, lucasMartins, marianaDias, nicolasTeixeira, oliviaBarbosa));
         */
        if (saob11 != null) {
            criarAusenciaSeNaoExistir(usuario, saob11, LocalDate.of(2025,8,19), "Congresso", "Trabalho");
            criarAusenciaSeNaoExistir(usuario, saob11, LocalDate.of(2025,9,9), "Show", "Pessoal");
        }


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

    private Contato criarContatoSeNaoExistir(Usuario usuario, String nome, String email, String telefone, Boolean pendente){
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
                    LocalDateTime agora = LocalDateTime.now();
                    novoContato.setDataSolicitacao(agora);
                    novoContato.setDataConfirmacao(pendente ? null : agora);
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
                            contato.setDataConfirmacao(LocalDateTime.now());
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

        if (reciproco == null) {
            Contato novoContato = new Contato();
            novoContato.setOwnerId(usuarioContato.getId());
            novoContato.setNome(usuarioOrigem.getNomeUsuario());
            novoContato.setEmail(usuarioOrigem.getEmail());
            novoContato.setPendente(false);
            novoContato.setIdContato(usuarioOrigem.getId());
            LocalDateTime agora = LocalDateTime.now();
            novoContato.setDataSolicitacao(agora);
            novoContato.setDataConfirmacao(agora);
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
            reciproco.setDataConfirmacao(LocalDateTime.now());
            atualizado = true;
        }
        if (reciproco.getDataSolicitacao() == null) {
            reciproco.setDataSolicitacao(LocalDateTime.now());
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
            // contato removido após o seed: ignore para não interromper a inicialização
        }
    }
}