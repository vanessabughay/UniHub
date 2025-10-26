package com.unihub.backend.service;

import com.unihub.backend.model.*;
import com.unihub.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompartilhamentoService {

    private final DisciplinaRepository disciplinaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ContatoRepository contatoRepository;
    private final ConviteCompartilhamentoRepository conviteRepository;
    private final NotificacaoRepository notificacaoRepository;

    public CompartilhamentoService(
            DisciplinaRepository disciplinaRepository,
            UsuarioRepository usuarioRepository,
            ContatoRepository contatoRepository,
            ConviteCompartilhamentoRepository conviteRepository,
            NotificacaoRepository notificacaoRepository
    ) {
        this.disciplinaRepository = disciplinaRepository;
        this.usuarioRepository = usuarioRepository;
        this.contatoRepository = contatoRepository;
        this.conviteRepository = conviteRepository;
        this.notificacaoRepository = notificacaoRepository;
    }

    public List<Usuario> listarContatos(Long usuarioId) {
        return contatoRepository.findByProprietarioId(usuarioId)
                .stream()
                .map(Contato::getContato)
                .collect(Collectors.toList());
    }

    @Transactional
    public Contato adicionarContato(Long proprietarioId, Long contatoId) {
        if (proprietarioId.equals(contatoId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível adicionar você mesmo como contato");
        }

        Usuario proprietario = usuarioRepository.findById(proprietarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário proprietário não encontrado"));
        Usuario contato = usuarioRepository.findById(contatoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário contato não encontrado"));

        if (contatoRepository.existsByProprietarioIdAndContatoId(proprietarioId, contatoId)) {
            return contatoRepository.findByProprietarioIdAndContatoId(proprietarioId, contatoId).get();
        }

        Contato novoContato = new Contato();
        novoContato.setProprietario(proprietario);
        novoContato.setContato(contato);
        return contatoRepository.save(novoContato);
    }

    @Transactional
    public ConviteCompartilhamento compartilharDisciplina(Long disciplinaId, Long remetenteId, Long destinatarioId, String mensagem) {
        Disciplina disciplina = disciplinaRepository.findById(disciplinaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina não encontrada"));

        Usuario remetente = usuarioRepository.findById(remetenteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Remetente não encontrado"));
        Usuario destinatario = usuarioRepository.findById(destinatarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destinatário não encontrado"));

        if (disciplina.getProprietario() == null) {
            disciplina.setProprietario(remetente);
            disciplinaRepository.save(disciplina);
        }

        if (!disciplina.getProprietario().getId().equals(remetenteId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "O usuário não é proprietário da disciplina");
        }

        if (!contatoRepository.existsByProprietarioIdAndContatoId(remetenteId, destinatarioId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contato não encontrado para o usuário informado");
        }

        Optional<ConviteCompartilhamento> conviteExistente = conviteRepository
                .findByDisciplinaIdAndDestinatarioIdAndStatus(disciplinaId, destinatarioId, StatusConvite.PENDENTE);
        if (conviteExistente.isPresent()) {
            return conviteExistente.get();
        }

        ConviteCompartilhamento convite = new ConviteCompartilhamento();
        convite.setDisciplina(disciplina);
        convite.setRemetente(remetente);
        convite.setDestinatario(destinatario);
        convite.setMensagem(mensagem);
        convite.setStatus(StatusConvite.PENDENTE);

        ConviteCompartilhamento salvo = conviteRepository.save(convite);

        Notificacao notificacao = new Notificacao();
        notificacao.setUsuario(destinatario);
        notificacao.setMensagem(String.format("%s deseja compartilhar a disciplina %s", remetente.getNome(), disciplina.getNome()));
        notificacao.setTipo(TipoNotificacao.COMPARTILHAMENTO);
        notificacao.setConvite(salvo);
        notificacaoRepository.save(notificacao);

        return salvo;
    }

    @Transactional
    public ConviteCompartilhamento aceitarConvite(Long conviteId, Long destinatarioId) {
        ConviteCompartilhamento convite = carregarConviteParaAcao(conviteId, destinatarioId);

        Disciplina novaDisciplina = clonarDisciplina(convite.getDisciplina(), convite.getDestinatario());
        disciplinaRepository.save(novaDisciplina);

        convite.setStatus(StatusConvite.ACEITO);
        convite.setRespondidoEm(java.time.LocalDateTime.now());
        ConviteCompartilhamento atualizado = conviteRepository.save(convite);

        marcarNotificacoesComoLidas(convite);

        return atualizado;
    }

    @Transactional
    public ConviteCompartilhamento rejeitarConvite(Long conviteId, Long destinatarioId) {
        ConviteCompartilhamento convite = carregarConviteParaAcao(conviteId, destinatarioId);
        convite.setStatus(StatusConvite.REJEITADO);
        convite.setRespondidoEm(java.time.LocalDateTime.now());
        ConviteCompartilhamento atualizado = conviteRepository.save(convite);

        marcarNotificacoesComoLidas(convite);

        return atualizado;
    }

    public List<Notificacao> listarNotificacoes(Long usuarioId) {
        return notificacaoRepository.findByUsuarioIdOrderByCriadaEmDesc(usuarioId);
    }

    private ConviteCompartilhamento carregarConviteParaAcao(Long conviteId, Long destinatarioId) {
        ConviteCompartilhamento convite = conviteRepository.findById(conviteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Convite não encontrado"));

        if (!convite.getDestinatario().getId().equals(destinatarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Convite não pertence ao usuário informado");
        }

        if (convite.getStatus() != StatusConvite.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Convite já respondido");
        }

        return convite;
    }

    private void marcarNotificacoesComoLidas(ConviteCompartilhamento convite) {
        notificacaoRepository.findByUsuarioIdOrderByCriadaEmDesc(convite.getDestinatario().getId())
                .stream()
                .filter(notificacao -> notificacao.getConvite() != null && notificacao.getConvite().getId().equals(convite.getId()))
                .forEach(notificacao -> {
                    notificacao.setLida(true);
                    notificacaoRepository.save(notificacao);
                });
    }

    private Disciplina clonarDisciplina(Disciplina original, Usuario novoProprietario) {
        Disciplina clone = new Disciplina();
        clone.setNome(original.getNome());
        clone.setProfessor(original.getProfessor());
        clone.setPeriodo(original.getPeriodo());
        clone.setCargaHoraria(original.getCargaHoraria());
        clone.setDataInicioSemestre(original.getDataInicioSemestre());
        clone.setDataFimSemestre(original.getDataFimSemestre());
        clone.setEmailProfessor(original.getEmailProfessor());
        clone.setPlataforma(original.getPlataforma());
        clone.setTelefoneProfessor(original.getTelefoneProfessor());
        clone.setSalaProfessor(original.getSalaProfessor());
        clone.setAtiva(original.isAtiva());
        clone.setReceberNotificacoes(original.isReceberNotificacoes());
        clone.setProprietario(novoProprietario);

        if (original.getAulas() != null) {
            List<HorarioAula> aulasClone = original.getAulas().stream().map(aulaOriginal -> {
                HorarioAula novaAula = new HorarioAula();
                novaAula.setDiaDaSemana(aulaOriginal.getDiaDaSemana());
                novaAula.setSala(aulaOriginal.getSala());
                novaAula.setHorarioInicio(aulaOriginal.getHorarioInicio());
                novaAula.setHorarioFim(aulaOriginal.getHorarioFim());
                return novaAula;
            }).collect(Collectors.toList());
            clone.setAulas(aulasClone);
        }

        if (original.getAvaliacoes() != null) {
            List<Avaliacao> avaliacoesClone = original.getAvaliacoes().stream().map(avaliacaoOriginal -> {
                Avaliacao novaAvaliacao = new Avaliacao();
                novaAvaliacao.setTitulo(avaliacaoOriginal.getTitulo());
                novaAvaliacao.setDescricao(avaliacaoOriginal.getDescricao());
                novaAvaliacao.setData(avaliacaoOriginal.getData());
                novaAvaliacao.setNota(null);
                return novaAvaliacao;
            }).collect(Collectors.toList());
            clone.setAvaliacoes(avaliacoesClone);
        }

        return clone;
    }
}
