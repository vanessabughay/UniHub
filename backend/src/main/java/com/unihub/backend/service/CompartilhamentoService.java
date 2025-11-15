package com.unihub.backend.service;

import com.unihub.backend.dto.compartilhamento.*;
import com.unihub.backend.model.*;
import com.unihub.backend.model.enums.StatusConviteCompartilhamento;
import com.unihub.backend.repository.ContatoRepository;
import com.unihub.backend.repository.ConviteCompartilhamentoRepository;
import com.unihub.backend.repository.DisciplinaRepository;
import com.unihub.backend.repository.NotificacaoRepository;
import com.unihub.backend.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompartilhamentoService {

    private static final String TIPO_CONVITE = "DISCIPLINA_COMPARTILHAMENTO";
    private static final String TIPO_RESPOSTA = "DISCIPLINA_COMPARTILHAMENTO_RESPOSTA";
    private static final String CATEGORIA_COMPARTILHAMENTO = "COMPARTILHAMENTO";
    private static final ZoneId ZONA_BRASIL = ZoneId.of("America/Sao_Paulo");

    private final ConviteCompartilhamentoRepository conviteRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ContatoRepository contatoRepository;
    private final NotificacaoRepository notificacaoRepository;

    public CompartilhamentoService(ConviteCompartilhamentoRepository conviteRepository,
                                   DisciplinaRepository disciplinaRepository,
                                   UsuarioRepository usuarioRepository,
                                   ContatoRepository contatoRepository,
                                   NotificacaoRepository notificacaoRepository) {
        this.conviteRepository = conviteRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.usuarioRepository = usuarioRepository;
        this.contatoRepository = contatoRepository;
        this.notificacaoRepository = notificacaoRepository;
    }

    @Transactional
    public ConviteCompartilhamentoResponse compartilhar(Long remetenteId,
                                                         CompartilharDisciplinaRequest request) {
        if (remetenteId == null) {
            throw new IllegalArgumentException("Usuário não autenticado");
        }
        if (request.getDestinatarioId() == null || Objects.equals(remetenteId, request.getDestinatarioId())) {
            throw new IllegalArgumentException("Destinatário inválido");
        }

        Disciplina disciplina = disciplinaRepository
                .findByIdAndUsuarioId(request.getDisciplinaId(), remetenteId)
                .orElseThrow(() -> new IllegalArgumentException("Disciplina não encontrada"));

        Usuario remetente = usuarioRepository.findById(remetenteId)
                .orElseThrow(() -> new IllegalArgumentException("Remetente não encontrado"));

        Usuario destinatario = usuarioRepository.findById(request.getDestinatarioId())
                .orElseThrow(() -> new IllegalArgumentException("Destinatário não encontrado"));

        validarContatoConfirmado(remetenteId, destinatario.getId());

        conviteRepository.findByDisciplinaIdAndDestinatarioIdAndStatus(
                        disciplina.getId(), destinatario.getId(), StatusConviteCompartilhamento.PENDENTE)
                .ifPresent(existing -> {
                    throw new IllegalStateException("Já existe um convite pendente para este contato");
                });

        ConviteCompartilhamento convite = new ConviteCompartilhamento();
        convite.setDisciplina(disciplina);
        convite.setRemetente(remetente);
        convite.setDestinatario(destinatario);
        convite.setMensagem(request.getMensagem());
        convite.setStatus(StatusConviteCompartilhamento.PENDENTE);
        convite.setCriadoEm(agora());
        conviteRepository.save(convite);

        String mensagem = String.format("%s quer compartilhar a disciplina %s",
                Optional.ofNullable(remetente.getNomeUsuario()).orElse("Um contato"),
                Optional.ofNullable(disciplina.getNome()).orElse("sem nome"));
        criarNotificacao(destinatario, convite, mensagem, TIPO_CONVITE);

        return ConviteCompartilhamentoResponse.fromEntity(convite);
    }

    @Transactional
    public ConviteCompartilhamentoResponse aceitarConvite(Long conviteId, Long usuarioId) {
        ConviteCompartilhamento convite = conviteRepository.findByIdAndDestinatarioId(conviteId, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Convite não encontrado"));

        if (convite.getStatus() != StatusConviteCompartilhamento.PENDENTE) {
            throw new IllegalStateException("Convite já respondido");
        }

        Disciplina disciplinaOriginal = convite.getDisciplina();
        if (disciplinaOriginal == null) {
            throw new IllegalStateException("Convite sem disciplina associada");
        }

        Usuario destinatario = convite.getDestinatario();
        if (destinatario == null || !Objects.equals(destinatario.getId(), usuarioId)) {
            throw new IllegalStateException("Destinatário inválido");
        }

        Disciplina disciplinaClonada = clonarDisciplina(disciplinaOriginal, destinatario);
        disciplinaRepository.save(disciplinaClonada);

        convite.setStatus(StatusConviteCompartilhamento.ACEITO);
        convite.setCriadoEm(agora());
        conviteRepository.save(convite);

        marcarNotificacoesComoLidas(convite.getId(), usuarioId);
        registrarRespostaParaDestinatario(convite, destinatario, true);

        Usuario remetente = convite.getRemetente();
        if (remetente != null) {
            String mensagem = String.format("%s aceitou o compartilhamento da disciplina %s",
                    Optional.ofNullable(destinatario.getNomeUsuario()).orElse("Seu contato"),
                    Optional.ofNullable(disciplinaOriginal.getNome()).orElse("sem nome"));
            criarNotificacao(remetente, convite, mensagem, TIPO_RESPOSTA);
        }

        return ConviteCompartilhamentoResponse.fromEntity(convite);
    }

    @Transactional
    public ConviteCompartilhamentoResponse rejeitarConvite(Long conviteId, Long usuarioId) {
        ConviteCompartilhamento convite = conviteRepository.findByIdAndDestinatarioId(conviteId, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Convite não encontrado"));

        if (convite.getStatus() != StatusConviteCompartilhamento.PENDENTE) {
            throw new IllegalStateException("Convite já respondido");
        }

        convite.setStatus(StatusConviteCompartilhamento.RECUSADO);
        convite.setRespondidoEm(agora());
        conviteRepository.save(convite);

        marcarNotificacoesComoLidas(convite.getId(), usuarioId);
        registrarRespostaParaDestinatario(convite, convite.getDestinatario(), false);

        Usuario remetente = convite.getRemetente();
        if (remetente != null) {
            String mensagem = String.format("%s recusou o compartilhamento da disciplina %s",
                    Optional.ofNullable(convite.getDestinatario()).map(Usuario::getNomeUsuario).orElse("Seu contato"),
                    Optional.ofNullable(convite.getDisciplina()).map(Disciplina::getNome).orElse("sem nome"));
            criarNotificacao(remetente, convite, mensagem, TIPO_RESPOSTA);
        }

        return ConviteCompartilhamentoResponse.fromEntity(convite);
    }

    public List<UsuarioResumoResponse> listarContatos(Long usuarioId) {
        List<Contato> contatos = contatoRepository.findByOwnerId(usuarioId);
        if (contatos.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> idsContatos = contatos.stream()
                .map(Contato::getIdContato)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (idsContatos.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Usuario> usuarios = usuarioRepository.findAllById(idsContatos).stream()
                .collect(Collectors.toMap(Usuario::getId, usuario -> usuario));

        return contatos.stream()
                .filter(contato -> Boolean.FALSE.equals(contato.getPendente()))
                .map(contato -> {
                    Long contatoId = contato.getIdContato();
                    if (contatoId == null) {
                        return null;
                    }
                    Usuario usuario = usuarios.get(contatoId);
String nomeContato = contato.getNome();
                    if (nomeContato == null || nomeContato.isBlank()) {
                        nomeContato = usuario != null ? usuario.getNomeUsuario() : contato.getEmail();
                    }
                    String email = usuario != null ? usuario.getEmail() : contato.getEmail();
                    return new UsuarioResumoResponse(contatoId, nomeContato, email);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<NotificacaoResponse> listarNotificacoes(Long usuarioId) {
         List<Notificacao> notificacoes = notificacaoRepository.findByUsuarioIdOrderByAtualizadaEmDesc(usuarioId);
         if (notificacoes.isEmpty()) {
            notificacoes = notificacaoRepository.findByUsuarioIdOrderByCriadaEmDesc(usuarioId);
         }

        return notificacoes.stream()
                .map(NotificacaoResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private void validarContatoConfirmado(Long ownerId, Long contatoId) {
        contatoRepository.findByOwnerIdAndIdContato(ownerId, contatoId)
                .filter(contato -> Boolean.FALSE.equals(contato.getPendente()))
                .orElseThrow(() -> new IllegalArgumentException("O destinatário precisa ser um contato confirmado"));
    }

    private void marcarNotificacoesComoLidas(Long conviteId, Long usuarioId) {
        List<Notificacao> notificacoes = notificacaoRepository.findByConviteIdAndUsuarioId(conviteId, usuarioId);
        if (notificacoes.isEmpty()) {
            return;
        }
        LocalDateTime momentoAtual = agora();
        notificacoes.forEach(notificacao -> {
            notificacao.setLida(true);
            notificacao.setInteracaoPendente(false);
            notificacao.setAtualizadaEm(momentoAtual);
        });
        notificacaoRepository.saveAll(notificacoes);
    }

    private void criarNotificacao(Usuario usuario, ConviteCompartilhamento convite,
                                  String mensagem, String tipo) {
        if (usuario == null) {
            return;
        }
        
        Long usuarioId = usuario.getId();
        Long referenciaId = convite != null ? convite.getId() : null;
        if (usuarioId != null && referenciaId != null) {
            Optional<Notificacao> existente = notificacaoRepository
                    .findByUsuarioIdAndTipoAndCategoriaAndReferenciaId(usuarioId, tipo,
                            CATEGORIA_COMPARTILHAMENTO, referenciaId);
            if (existente.isPresent()) {
                Notificacao notificacaoExistente = existente.get();
                notificacaoExistente.setTitulo(definirTituloNotificacao(tipo, convite));
                notificacaoExistente.setMensagem(mensagem);
                boolean pendente = TIPO_CONVITE.equals(tipo) && !notificacaoExistente.isLida();
                notificacaoExistente.setInteracaoPendente(pendente);
                if (pendente) {
                    notificacaoExistente.setLida(false);
                }
                notificacaoExistente.setAtualizadaEm(agora());
                notificacaoRepository.save(notificacaoExistente);
                return;
            }
        }

        Notificacao notificacao = new Notificacao();
        notificacao.setUsuario(usuario);
        notificacao.setConvite(convite);
        notificacao.setTitulo(definirTituloNotificacao(tipo, convite));
        notificacao.setMensagem(mensagem);
        notificacao.setTipo(tipo);
        notificacao.setCategoria(CATEGORIA_COMPARTILHAMENTO);
        notificacao.setReferenciaId(referenciaId);
        notificacao.setInteracaoPendente(TIPO_CONVITE.equals(tipo));
        LocalDateTime agoraNotificacao = agora();
        notificacao.setCriadaEm(agoraNotificacao);
        notificacao.setAtualizadaEm(agoraNotificacao);
        notificacao.setLida(false);
        notificacaoRepository.save(notificacao);
    }

    private void registrarRespostaParaDestinatario(ConviteCompartilhamento convite,
                                                   Usuario destinatario,
                                                   boolean aceito) {
        if (destinatario == null || destinatario.getId() == null) {
            return;
        }

        Long referenciaId = convite != null ? convite.getId() : null;
        Optional<Notificacao> existente = Optional.empty();
        if (referenciaId != null) {
            existente = notificacaoRepository.findByUsuarioIdAndTipoAndCategoriaAndReferenciaId(
                    destinatario.getId(),
                    TIPO_RESPOSTA,
                    CATEGORIA_COMPARTILHAMENTO,
                    referenciaId
            );
        }

        Notificacao notificacao = existente.orElseGet(Notificacao::new);
        boolean nova = notificacao.getId() == null;

        notificacao.setUsuario(destinatario);
        notificacao.setConvite(convite);
        notificacao.setTipo(TIPO_RESPOSTA);
        notificacao.setCategoria(CATEGORIA_COMPARTILHAMENTO);
        notificacao.setReferenciaId(referenciaId);
        notificacao.setInteracaoPendente(false);

        String disciplinaNome = Optional.ofNullable(convite)
                .map(ConviteCompartilhamento::getDisciplina)
                .map(Disciplina::getNome)
                .filter(nome -> nome != null && !nome.isBlank())
                .orElse("sua disciplina");

        String titulo = aceito ? "Compartilhamento aceito" : "Compartilhamento recusado";
        String mensagem = aceito
                ? String.format("Você aceitou o compartilhamento da disciplina %s.", disciplinaNome)
                : String.format("Você recusou o compartilhamento da disciplina %s.", disciplinaNome);

        notificacao.setTitulo(titulo);
        notificacao.setMensagem(mensagem);

        LocalDateTime agora = agora();
        if (nova) {
            notificacao.setCriadaEm(agora);
            notificacao.setLida(false);
        }
        notificacao.setAtualizadaEm(agora);

        notificacaoRepository.save(notificacao);
    }
    
     private String definirTituloNotificacao(String tipo, ConviteCompartilhamento convite) {
        String disciplinaNome = Optional.ofNullable(convite)
                .map(ConviteCompartilhamento::getDisciplina)
                .map(Disciplina::getNome)
                .filter(nome -> nome != null && !nome.isBlank())
                .orElse("sua disciplina");
        if (TIPO_CONVITE.equals(tipo)) {
             String remetenteNome = resolverNomeUsuario(
                    Optional.ofNullable(convite).map(ConviteCompartilhamento::getRemetente).orElse(null),
                    "Um contato");
            return String.format("%s compartilhou %s", remetenteNome, disciplinaNome);
        }
        if (TIPO_RESPOSTA.equals(tipo)) {
            String respondenteNome = resolverNomeUsuario(
                    Optional.ofNullable(convite).map(ConviteCompartilhamento::getDestinatario).orElse(null),
                    "Seu contato");
            return String.format("%s respondeu sobre %s", respondenteNome, disciplinaNome);
        }
        return tipo;
    }


    private String resolverNomeUsuario(Usuario usuario, String fallback) {
        if (usuario == null) {
            return fallback;
        }
        String nome = usuario.getNomeUsuario();
        if (nome != null && !nome.isBlank()) {
            return nome;
        }
        String email = usuario.getEmail();
        if (email != null && !email.isBlank()) {
            return email;
        }
        return fallback;
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(ZONA_BRASIL);
    }
    
    private Disciplina clonarDisciplina(Disciplina original, Usuario novoUsuario) {
        Disciplina copia = new Disciplina();
        copia.setCodigo(original.getCodigo());
        copia.setNome(original.getNome());
        copia.setProfessor(original.getProfessor());
        copia.setPeriodo(original.getPeriodo());
        copia.setCargaHoraria(original.getCargaHoraria());
        copia.setQtdSemanas(original.getQtdSemanas());
        copia.setDataInicioSemestre(original.getDataInicioSemestre());
        copia.setDataFimSemestre(original.getDataFimSemestre());
        copia.setEmailProfessor(original.getEmailProfessor());
        copia.setPlataforma(original.getPlataforma());
        copia.setTelefoneProfessor(original.getTelefoneProfessor());
        copia.setSalaProfessor(original.getSalaProfessor());
        copia.setAtiva(original.isAtiva());
        copia.setReceberNotificacoes(original.isReceberNotificacoes());
        copia.setAusenciasPermitidas(original.getAusenciasPermitidas());
        copia.setUsuario(novoUsuario);

        if (original.getAulas() != null) {
            original.getAulas().forEach(aulaOriginal -> {
                HorarioAula aula = new HorarioAula();
                aula.setDiaDaSemana(aulaOriginal.getDiaDaSemana());
                aula.setSala(aulaOriginal.getSala());
                aula.setHorarioInicio(aulaOriginal.getHorarioInicio());
                aula.setHorarioFim(aulaOriginal.getHorarioFim());
                copia.addAula(aula);
            });
        }

        List<Avaliacao> avaliacoesOriginais = Optional.ofNullable(original.getAvaliacoes())
                .orElseGet(Collections::emptyList);

        avaliacoesOriginais.forEach(avaliacaoOriginal -> {
            Avaliacao novaAvaliacao = new Avaliacao();
            novaAvaliacao.setDescricao(avaliacaoOriginal.getDescricao());
            novaAvaliacao.setTipoAvaliacao(avaliacaoOriginal.getTipoAvaliacao());
            novaAvaliacao.setDataEntrega(avaliacaoOriginal.getDataEntrega());
            novaAvaliacao.setPrioridade(avaliacaoOriginal.getPrioridade());
            novaAvaliacao.setEstado(avaliacaoOriginal.getEstado());
            novaAvaliacao.setModalidade(avaliacaoOriginal.getModalidade());
            novaAvaliacao.setDificuldade(avaliacaoOriginal.getDificuldade());
            novaAvaliacao.setReceberNotificacoes(avaliacaoOriginal.getReceberNotificacoes());
            novaAvaliacao.setUsuario(novoUsuario);
            novaAvaliacao.setNota(null);
            novaAvaliacao.setPeso(null);
            copia.addAvaliacao(novaAvaliacao);
        });

        return copia;
    }
}