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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompartilhamentoService {

    private static final String TIPO_CONVITE = "DISCIPLINA_COMPARTILHAMENTO";
    private static final String TIPO_RESPOSTA = "DISCIPLINA_COMPARTILHAMENTO_RESPOSTA";

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
        convite.setCriadoEm(LocalDateTime.now());
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
        convite.setRespondidoEm(LocalDateTime.now());
        conviteRepository.save(convite);

        marcarNotificacoesComoLidas(convite.getId(), usuarioId);

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
        convite.setRespondidoEm(LocalDateTime.now());
        conviteRepository.save(convite);

        marcarNotificacoesComoLidas(convite.getId(), usuarioId);

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
                    String nome = usuario != null ? usuario.getNomeUsuario() : contato.getNome();
                    String email = usuario != null ? usuario.getEmail() : contato.getEmail();
                    return new UsuarioResumoResponse(contatoId, nome, email);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<NotificacaoResponse> listarNotificacoes(Long usuarioId) {
        return notificacaoRepository.findByUsuarioIdOrderByCriadaEmDesc(usuarioId).stream()
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
        notificacoes.forEach(notificacao -> notificacao.setLida(true));
        notificacaoRepository.saveAll(notificacoes);
    }

    private void criarNotificacao(Usuario usuario, ConviteCompartilhamento convite,
                                  String mensagem, String tipo) {
        if (usuario == null) {
            return;
        }
        Notificacao notificacao = new Notificacao();
        notificacao.setUsuario(usuario);
        notificacao.setConvite(convite);
        notificacao.setMensagem(mensagem);
        notificacao.setTipo(tipo);
        notificacao.setCriadaEm(LocalDateTime.now());
        notificacaoRepository.save(notificacao);
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