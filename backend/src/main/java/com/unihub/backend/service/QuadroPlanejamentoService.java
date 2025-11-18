package com.unihub.backend.service;

import com.unihub.backend.dto.planejamento.AtualizarPreferenciaTarefaRequest;
import com.unihub.backend.dto.planejamento.AtualizarStatusTarefaRequest;
import com.unihub.backend.dto.planejamento.AtualizarTarefaPlanejamentoRequest;
import com.unihub.backend.dto.planejamento.ColunaPlanejamentoRequest;
import com.unihub.backend.dto.planejamento.PreferenciaTarefaResponse;
import com.unihub.backend.dto.planejamento.QuadroPlanejamentoDetalhesResponse;
import com.unihub.backend.dto.planejamento.QuadroPlanejamentoListaResponse;
import com.unihub.backend.dto.planejamento.QuadroPlanejamentoRequest;
import com.unihub.backend.dto.planejamento.TarefaComentarioRequest;
import com.unihub.backend.dto.planejamento.TarefaComentarioResponse;
import com.unihub.backend.dto.planejamento.TarefaComentariosResponse;
import com.unihub.backend.dto.planejamento.TarefaPlanejamentoRequest;
import com.unihub.backend.dto.planejamento.TarefaPlanejamentoResponse;
import com.unihub.backend.exceptions.ResourceNotFoundException;
import com.unihub.backend.model.*;
import com.unihub.backend.model.enums.EstadoPlanejamento;
import com.unihub.backend.model.enums.QuadroStatus;
import com.unihub.backend.model.enums.TarefaStatus;
import com.unihub.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unihub.backend.dto.planejamento.TarefaDto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuadroPlanejamentoService {

    @Autowired
    private QuadroPlanejamentoRepository repository;
    @Autowired
    private ColunaPlanejamentoRepository colunaRepository;
    @Autowired
    private TarefaPlanejamentoRepository tarefaRepository;
    @Autowired
    private ContatoRepository contatoRepository;
    @Autowired
    private GrupoRepository grupoRepository;
    // add pra disciplina e usuario
    @Autowired
    private DisciplinaRepository disciplinaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TarefaComentarioRepository tarefaComentarioRepository;
    @Autowired
    private TarefaNotificacaoRepository tarefaNotificacaoRepository;
    @Autowired
    private NotificacaoRepository notificacaoRepository;
    @Autowired
    private NotificacaoConfiguracaoRepository notificacaoConfiguracaoRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private static final String NOTIFICACAO_TIPO = "APP_NOTIFICACAO";
    private static final String NOTIFICACAO_CATEGORIA = "QUADRO_PLANEJAMENTO";
    private static final String NOTIFICACAO_TITULO = "Quadro compartilhado";
    private static final String NOTIFICACAO_TAREFA_ATRIBUIDA_CATEGORIA = "TAREFA_ATRIBUIDA";
    private static final String NOTIFICACAO_TAREFA_ATRIBUIDA_TITULO = "Tarefa atribuída";
    private static final String NOTIFICACAO_TAREFA_COMENTARIO_CATEGORIA = "TAREFA_COMENTARIO";
    private static final String NOTIFICACAO_TAREFA_COMENTARIO_TITULO = "Novo comentário na tarefa";
    private static final ZoneId ZONA_BRASIL = ZoneId.of("America/Sao_Paulo");

    public List<QuadroPlanejamentoListaResponse> listar(Long usuarioId, QuadroStatus status, String titulo) {
        String tituloNormalizado = titulo != null ? titulo.trim() : null;
        List<QuadroPlanejamento> quadros = repository.findAllAccessibleByUsuarioId(usuarioId);

        return quadros.stream()
                .filter(quadro -> status == null || quadro.getStatus() == status)
                .filter(quadro -> {
                    if (tituloNormalizado == null || tituloNormalizado.isEmpty()) {
                        return true;
                    }
                    String tituloQuadro = quadro.getTitulo();
                    return tituloQuadro != null && tituloQuadro.toLowerCase(Locale.ROOT).contains(tituloNormalizado.toLowerCase(Locale.ROOT));
                })
                .map(QuadroPlanejamentoListaResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public QuadroPlanejamento buscarPorId(Long id, Long usuarioId) {
        return repository.findByIdAndUsuarioHasAccess(id, usuarioId)
                        .orElseThrow(() -> new ResourceNotFoundException("Quadro de Planejamento não encontrado"));
    }

    // criar atualizado
    @Transactional
    public QuadroPlanejamento criar(QuadroPlanejamentoRequest dto, Long usuarioId) {
        // Validação da regra de negócio
        if (dto.getContatoId() != null && dto.getGrupoId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um quadro só pode ter um contato ou um grupo como integrante, não ambos.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        QuadroPlanejamento quadro = new QuadroPlanejamento();
        quadro.setUsuario(usuario);
        quadro.setTitulo(dto.getNome());
        
        // Busca e associa as entidades relacionadas pelos IDs do DTO
        if (dto.getDisciplinaId() != null) {
            Disciplina disciplina = disciplinaRepository.findById(dto.getDisciplinaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Disciplina não encontrada"));
            quadro.setDisciplina(disciplina);
        }
        if (dto.getContatoId() != null) {
                Contato contato = obterContatoDoUsuario(usuarioId, dto.getContatoId());
            quadro.setContato(contato);
        }
        if (dto.getGrupoId() != null) {
            Grupo grupo = grupoRepository.findById(dto.getGrupoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado"));
            quadro.setGrupo(grupo);
        }


        if (dto.getDataFim() != null) {
            quadro.setDataPrazo(LocalDateTime.ofInstant(Instant.ofEpochMilli(dto.getDataFim()), ZoneId.systemDefault()));
        }
        ajustarStatus(quadro, null); 
        
        QuadroPlanejamento salvo = repository.save(quadro);
        Set<Long> novosParticipantes = extrairParticipantes(salvo);
        notificarNovosParticipantes(salvo, novosParticipantes);
        return salvo;
    }

    // atualizar atualizado
    @Transactional
    public QuadroPlanejamento atualizar(Long id, QuadroPlanejamentoRequest dto, Long usuarioId) {
        QuadroPlanejamento existente = buscarPorId(id, usuarioId);
        QuadroStatus statusAnterior = existente.getStatus();
        Set<Long> participantesAntes = extrairParticipantes(existente);
        
        // Validação da regra de negócio
        if (dto.getContatoId() != null && dto.getGrupoId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um quadro só pode ter um contato ou um grupo como integrante, não ambos.");
        }

        existente.setTitulo(dto.getNome());
        if (dto.getDataFim() != null) {
            existente.setDataPrazo(LocalDateTime.ofInstant(Instant.ofEpochMilli(dto.getDataFim()), ZoneId.systemDefault()));
        } else {
            existente.setDataPrazo(null);
        }

        if (dto.getEstado() != null) {
            existente.setStatus(dto.getEstado());
        }

        // Atualiza as relações
        if (dto.getDisciplinaId() != null) {
            Disciplina disciplina = disciplinaRepository.findById(dto.getDisciplinaId()).orElseThrow(() -> new ResourceNotFoundException("Disciplina não encontrada"));
            existente.setDisciplina(disciplina);
        } else {
            existente.setDisciplina(null);
        }

        if (dto.getContatoId() != null) {
            Contato contato = obterContatoDoUsuario(usuarioId, dto.getContatoId());
            existente.setContato(contato);
            existente.setGrupo(null); // Garante exclusividade
        } else if (dto.getGrupoId() != null) {
             Grupo grupo = grupoRepository.findById(dto.getGrupoId()).orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado"));
            existente.setGrupo(grupo);
            existente.setContato(null); // Garante exclusividade
        } else {
            existente.setContato(null);
            existente.setGrupo(null);
        }
        
        ajustarStatus(existente, statusAnterior);

        QuadroPlanejamento salvo = repository.save(existente);
        Set<Long> participantesDepois = extrairParticipantes(salvo);
        participantesDepois.removeAll(participantesAntes);
        notificarNovosParticipantes(salvo, participantesDepois);

        return salvo;
    }

    @Transactional
    public void excluir(Long id, Long usuarioId) {
        QuadroPlanejamento quadro = buscarPorId(id, usuarioId);
        repository.delete(quadro);
    }

    // detalhes atualizado
    @Transactional(readOnly = true)
    public QuadroPlanejamentoDetalhesResponse detalhes(Long id, Long usuarioId) {
        QuadroPlanejamento quadro = buscarPorId(id, usuarioId);
        List<ColunaPlanejamento> colunas = colunaRepository.findByQuadroIdOrderByOrdemAsc(quadro.getId());

        List<ColunaPlanejamento> andamento = colunas.stream()
                .filter(coluna -> coluna.getEstado() == EstadoPlanejamento.EM_ANDAMENTO)
                .collect(Collectors.toList());

        List<ColunaPlanejamento> concluidas = colunas.stream()
                .filter(coluna -> coluna.getEstado() == EstadoPlanejamento.CONCLUIDO)
                .collect(Collectors.toList());

        QuadroPlanejamentoDetalhesResponse response = new QuadroPlanejamentoDetalhesResponse();
        response.setId(quadro.getId());
        response.setNome(quadro.getTitulo());
        response.setEstado(quadro.getStatus());
        response.setDataFim(quadro.getDataPrazo());
        response.setDonoId(quadro.getDonoId());
        
        // Mapeia os novos campos de ID para a resposta
        response.setDisciplinaId(quadro.getDisciplinaId());
        response.setContatoId(quadro.getContatoId());
        response.setGrupoId(quadro.getGrupoId());
        
        response.setColunasEmAndamento(andamento);
        response.setColunasConcluidas(concluidas);

        return response;
    }


    @Transactional
    public ColunaPlanejamento criarColuna(Long quadroId, ColunaPlanejamentoRequest request, Long usuarioId) {
        QuadroPlanejamento quadro = buscarPorId(quadroId, usuarioId);
        validarTitulo(request.getTitulo());

        ColunaPlanejamento coluna = new ColunaPlanejamento();
        coluna.setTitulo(request.getTitulo());
        coluna.setEstado(request.getEstado() != null ? request.getEstado() : EstadoPlanejamento.EM_ANDAMENTO);
        Integer maiorOrdem = colunaRepository.findMaxOrdemByQuadroId(quadro.getId());
        int proximaOrdem = (maiorOrdem != null ? maiorOrdem : 0) + 1;
        coluna.setOrdem(proximaOrdem);
        coluna.setQuadro(quadro);

        return colunaRepository.save(coluna);
    }

    @Transactional(readOnly = true)
    public ColunaPlanejamento buscarColunaPorId(Long quadroId, Long colunaId, Long usuarioId) {
        return buscarColuna(quadroId, colunaId, usuarioId);
    }

    @Transactional
    public ColunaPlanejamento atualizarColuna(Long quadroId, Long colunaId, ColunaPlanejamentoRequest request, Long usuarioId) {
        ColunaPlanejamento coluna = buscarColuna(quadroId, colunaId, usuarioId);

        if (request.getTitulo() != null) {
            validarTitulo(request.getTitulo());
            coluna.setTitulo(request.getTitulo());
        }

        if (request.getEstado() != null) {
            coluna.setEstado(request.getEstado());
        }

        if (request.getOrdem() != null) {
            coluna.setOrdem(request.getOrdem());
        }

        return colunaRepository.save(coluna);
    }

    @Transactional
    public void excluirColuna(Long quadroId, Long colunaId, Long usuarioId) {
        ColunaPlanejamento coluna = buscarColuna(quadroId, colunaId, usuarioId);
        colunaRepository.delete(coluna);
    }

    @Transactional(readOnly = true)
    public List<ColunaPlanejamento> listarColunas(Long quadroId, EstadoPlanejamento estado, Long usuarioId) {
        buscarPorId(quadroId, usuarioId);
        if (estado == null) {
            return colunaRepository.findByQuadroIdOrderByOrdemAsc(quadroId);
        }
        return colunaRepository.findByQuadroIdAndEstadoOrderByOrdemAsc(quadroId, estado);
    }

    @Transactional(readOnly = true)
    public List<TarefaPlanejamento> listarTarefas(Long quadroId, Long colunaId, Long usuarioId) {
        ColunaPlanejamento coluna = buscarColuna(quadroId, colunaId, usuarioId);
        return tarefaRepository.findByColunaIdOrderByDataPrazoAsc(coluna.getId());
    }

    @Transactional(readOnly = true)
    public TarefaPlanejamentoResponse buscarTarefa(Long quadroId, Long colunaId, Long tarefaId, Long usuarioId) {
        TarefaPlanejamento tarefa = buscarTarefaEntity(quadroId, colunaId, tarefaId, usuarioId);
        return toResponse(tarefa);
    }

    @Transactional
    public TarefaPlanejamento criarTarefa(Long quadroId, Long colunaId, TarefaPlanejamentoRequest request, Long usuarioId) {
        ColunaPlanejamento coluna = buscarColuna(quadroId, colunaId, usuarioId);
        validarTitulo(request.getTitulo());

        TarefaPlanejamento tarefa = new TarefaPlanejamento();
        tarefa.setTitulo(request.getTitulo());
        tarefa.setDescricao(request.getDescricao());
        if (request.getDataPrazo() != null && !request.getDataPrazo().isBlank()) {
            tarefa.setDataPrazo(parsePrazo(request.getDataPrazo()));
        } else {
            tarefa.setDataPrazo(null);
        }
        tarefa.setColuna(coluna);

        tarefa.setResponsaveis(buscarResponsaveis(quadroId, request.getResponsavelIds(), usuarioId));

        TarefaPlanejamento salvo = tarefaRepository.save(tarefa);
        Set<Long> novosResponsaveis = extrairResponsaveisIds(salvo);
        notificarResponsaveisAtribuidos(salvo, novosResponsaveis, usuarioId);
        sincronizarInscricoesComentarios(salvo, novosResponsaveis);
        return salvo;
    }

    @Transactional
    public TarefaPlanejamentoResponse atualizarTarefa(Long quadroId, Long colunaId, Long tarefaId,
                                                      AtualizarTarefaPlanejamentoRequest request, Long usuarioId) {
        TarefaPlanejamento tarefa = buscarTarefaEntity(quadroId, colunaId, tarefaId, usuarioId);
        Set<Long> responsaveisAntes = extrairResponsaveisIds(tarefa);

        if (request.getTitulo() != null) {
            validarTitulo(request.getTitulo());
            tarefa.setTitulo(request.getTitulo());
        }

        tarefa.setDescricao(request.getDescricao());

        if (request.getPrazo() != null && !request.getPrazo().isBlank()) {
            tarefa.setDataPrazo(parsePrazo(request.getPrazo()));
        } else {
            tarefa.setDataPrazo(null);
        }

        if ("CONCLUIDA".equalsIgnoreCase(request.getStatus())) {
            tarefa.setStatus(TarefaStatus.CONCLUIDA);
        } else {
            tarefa.setStatus(TarefaStatus.PENDENTE);
        }

        if (request.getResponsavelIds() != null) {
            tarefa.setResponsaveis(buscarResponsaveis(quadroId, request.getResponsavelIds(), usuarioId));
        }

        TarefaPlanejamento atualizado = tarefaRepository.save(tarefa);
         Set<Long> responsaveisDepois = extrairResponsaveisIds(atualizado);

        Set<Long> novosResponsaveis = new LinkedHashSet<>(responsaveisDepois);
        novosResponsaveis.removeAll(responsaveisAntes);
        if (!novosResponsaveis.isEmpty()) {
            notificarResponsaveisAtribuidos(atualizado, novosResponsaveis, usuarioId);
            sincronizarInscricoesComentarios(atualizado, novosResponsaveis);
        }

        Set<Long> removidos = new LinkedHashSet<>(responsaveisAntes);
        removidos.removeAll(responsaveisDepois);
        if (!removidos.isEmpty()) {
            removerInscricoesComentarios(atualizado, removidos);
        }
        
        return toResponse(atualizado);
    }

    @Transactional
    public TarefaPlanejamento atualizarStatusTarefa(Long quadroId, Long tarefaId, AtualizarStatusTarefaRequest request, Long usuarioId) {
        TarefaPlanejamento tarefa = tarefaRepository.findByIdAndColunaQuadroId(tarefaId, quadroId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada"));

        buscarPorId(quadroId, usuarioId);

        if (request.isConcluida()) {
            tarefa.setStatus(TarefaStatus.CONCLUIDA);
        } else {
            tarefa.setStatus(TarefaStatus.PENDENTE);
        }

        return tarefaRepository.save(tarefa);
    }

    @Transactional
    public void excluirTarefa(Long quadroId, Long colunaId, Long tarefaId, Long usuarioId) {
        TarefaPlanejamento tarefa = buscarTarefaEntity(quadroId, colunaId, tarefaId, usuarioId);
        tarefaRepository.delete(tarefa);
    }
    @Transactional(readOnly = true)
    public TarefaComentariosResponse listarComentarios(Long quadroId, Long colunaId, Long tarefaId, Long usuarioId) {
        TarefaPlanejamento tarefa = buscarTarefaEntity(quadroId, colunaId, tarefaId, usuarioId);

        List<TarefaComentarioResponse> comentarios = tarefaComentarioRepository
                .findByTarefaOrderByDataCriacaoDesc(tarefa)
                .stream()
                .map(comentario -> toComentarioResponse(comentario, usuarioId))
                .collect(Collectors.toList());

        boolean receberNotificacoes = tarefaNotificacaoRepository
            .existsByTarefaIdAndUsuarioId(tarefa.getId(), usuarioId);

        TarefaComentariosResponse response = new TarefaComentariosResponse();
        response.setComentarios(comentarios);
        response.setReceberNotificacoes(receberNotificacoes);
        return response;
    }

    @Transactional
    public TarefaComentarioResponse adicionarComentario(Long quadroId, Long colunaId, Long tarefaId,
                                                        TarefaComentarioRequest request, Long usuarioId) {
        if (request.getConteudo() == null || request.getConteudo().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O comentário não pode ser vazio.");
        }

        TarefaPlanejamento tarefa = buscarTarefaEntity(quadroId, colunaId, tarefaId, usuarioId);
        Usuario autor = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        TarefaComentario comentario = new TarefaComentario();
        comentario.setTarefa(tarefa);
        comentario.setAutor(autor);
        comentario.setConteudo(request.getConteudo().trim());

        TarefaComentario salvo = tarefaComentarioRepository.save(comentario);
        notificarResponsaveisSobreComentario(tarefa, salvo);
        return toComentarioResponse(salvo, usuarioId);
    }

    @Transactional
    public TarefaComentarioResponse atualizarComentario(Long quadroId, Long colunaId, Long tarefaId, Long comentarioId,
                                                        TarefaComentarioRequest request, Long usuarioId) {
        if (request.getConteudo() == null || request.getConteudo().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O comentário não pode ser vazio.");
        }

        TarefaPlanejamento tarefa = buscarTarefaEntity(quadroId, colunaId, tarefaId, usuarioId);
        TarefaComentario comentario = tarefaComentarioRepository.findByIdAndTarefaId(comentarioId, tarefa.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Comentário não encontrado"));

        if (!comentario.getAutor().getId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para editar este comentário.");
        }

        comentario.setConteudo(request.getConteudo().trim());
        TarefaComentario atualizado = tarefaComentarioRepository.save(comentario);
        return toComentarioResponse(atualizado, usuarioId);
    }

    @Transactional
    public void excluirComentario(Long quadroId, Long colunaId, Long tarefaId, Long comentarioId, Long usuarioId) {
        TarefaPlanejamento tarefa = buscarTarefaEntity(quadroId, colunaId, tarefaId, usuarioId);
        TarefaComentario comentario = tarefaComentarioRepository.findByIdAndTarefaId(comentarioId, tarefa.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Comentário não encontrado"));

        if (!comentario.getAutor().getId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para excluir este comentário.");
        }

        tarefaComentarioRepository.delete(comentario);
    }

    @Transactional
    public PreferenciaTarefaResponse atualizarPreferenciaTarefa(Long quadroId, Long colunaId, Long tarefaId,
                                                                AtualizarPreferenciaTarefaRequest request,
                                                                Long usuarioId) {
        TarefaPlanejamento tarefa = buscarTarefaEntity(quadroId, colunaId, tarefaId, usuarioId);

        boolean desejaReceber = request.isReceberNotificacoes();
        if (desejaReceber) {
                        boolean jaExiste = tarefaNotificacaoRepository
                    .existsByTarefaIdAndUsuarioId(tarefa.getId(), usuarioId);
            if (!jaExiste) {
                Usuario usuario = usuarioRepository.findById(usuarioId)
                        .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
                TarefaNotificacao notificacao = new TarefaNotificacao();
                notificacao.setTarefa(tarefa);
                notificacao.setUsuario(usuario);
                tarefaNotificacaoRepository.save(notificacao);
            }
        } else {
            tarefaNotificacaoRepository.deleteByTarefaIdAndUsuarioId(tarefa.getId(), usuarioId);
        }

        boolean estadoAtual = tarefaNotificacaoRepository
                .existsByTarefaIdAndUsuarioId(tarefa.getId(), usuarioId);
                return new PreferenciaTarefaResponse(estadoAtual);
    }

    private void ajustarStatus(QuadroPlanejamento quadro, QuadroStatus statusAnterior) {
        if (quadro.getStatus() == null) {
            quadro.setStatus(QuadroStatus.ATIVO);
        }

        if (quadro.getStatus() == QuadroStatus.ENCERRADO && quadro.getDataPrazo() == null) {
            quadro.setDataPrazo(LocalDateTime.now());
        }
    }

    private void validarTitulo(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("Título é obrigatório");
        }
    }

    private ColunaPlanejamento buscarColuna(Long quadroId, Long colunaId, Long usuarioId) {
        buscarPorId(quadroId, usuarioId);
        return colunaRepository.findByIdAndQuadroId(colunaId, quadroId)
                .orElseThrow(() -> new ResourceNotFoundException("Coluna não encontrada"));
    }

    private TarefaPlanejamento buscarTarefaEntity(Long quadroId, Long colunaId, Long tarefaId, Long usuarioId) {
        ColunaPlanejamento coluna = buscarColuna(quadroId, colunaId, usuarioId);
        return tarefaRepository.findByIdAndColunaId(tarefaId, coluna.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada"));
    }

    private TarefaPlanejamentoResponse toResponse(TarefaPlanejamento tarefa) {
        TarefaPlanejamentoResponse response = new TarefaPlanejamentoResponse();
        response.setId(tarefa.getId());
        response.setTitulo(tarefa.getTitulo());
        response.setDescricao(tarefa.getDescricao());
        response.setStatus(tarefa.getStatus() == TarefaStatus.CONCLUIDA ? "CONCLUIDA" : "INICIADA");
        response.setPrazo(formatDateTimeToIso(tarefa.getDataPrazo()));
        response.setResponsavelIds(tarefa.getResponsaveisIds());
        response.setResponsaveis(tarefa.getResponsaveisIdsRegistrados());
        return response;
    }

    private TarefaComentarioResponse toComentarioResponse(TarefaComentario comentario, Long usuarioId) {
        TarefaComentarioResponse response = new TarefaComentarioResponse();
        response.setId(comentario.getId());
        response.setConteudo(comentario.getConteudo());
        response.setAutorId(comentario.getAutor().getId());
        response.setAutorNome(comentario.getAutor().getNomeUsuario());
        response.setAutor(comentario.getAutor().getId().equals(usuarioId));

        LocalDateTime criado = comentario.getDataCriacao();
        LocalDateTime atualizado = comentario.getDataAtualizacao();
        if (criado != null) {
            response.setDataCriacao(criado.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        if (atualizado != null) {
            response.setDataAtualizacao(atualizado.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        return response;
    }


    private String formatDateTimeToIso(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private LocalDateTime parsePrazo(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        String trimmed = valor.trim();
        return tryParseInstant(trimmed)
                .or(() -> tryParseOffsetDateTime(trimmed))
                .or(() -> tryParseEpochMillis(trimmed))
                .or(() -> tryParseLocalDateTime(trimmed))
                .or(() -> tryParseLocalDate(trimmed))
                .orElseThrow(() -> new IllegalArgumentException("Formato de prazo inválido: " + valor));
    }

        private Optional<LocalDateTime> tryParseInstant(String valor) {
        try {
            Instant instant = Instant.parse(valor);
            return Optional.of(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
        } catch (DateTimeParseException ignored) {
            return Optional.empty();        
        }
    }

     private Optional<LocalDateTime> tryParseOffsetDateTime(String valor) {
        try {
            return Optional.of(OffsetDateTime.parse(valor).toLocalDateTime());
        } catch (DateTimeParseException ignored) {
            return Optional.empty();
        }
    }

    private Optional<LocalDateTime> tryParseEpochMillis(String valor) {
        if (!valor.matches("^-?\\d+$")) {
            return Optional.empty();
        }
        try {
            long epochMillis = Long.parseLong(valor);
            Instant instant = Instant.ofEpochMilli(epochMillis);
            return Optional.of(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

        private Optional<LocalDateTime> tryParseLocalDateTime(String valor) {
        String normalized = valor.replace(' ', 'T');
        if (normalized.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}$")) {
            normalized = normalized + ":00";
        }

        try {
            return Optional.of(LocalDateTime.parse(normalized));
        } catch (DateTimeParseException ignored) {
                        return Optional.empty();
        }
    }

    private Optional<LocalDateTime> tryParseLocalDate(String valor) {
        try {
            LocalDate date = LocalDate.parse(valor);
            return Optional.of(date.atStartOfDay());
        } catch (DateTimeParseException ignored) {
            return Optional.empty();
        }

    }


    private Contato buscarContato(Long quadroId, Long usuarioContatoId, Long usuarioId) {
        QuadroPlanejamento quadro = buscarPorId(quadroId, usuarioId);

        return obterContatoDoUsuario(quadro, usuarioId, usuarioContatoId);
    }

    private LinkedHashSet<Contato> buscarResponsaveis(Long quadroId, List<Long> responsavelIds, Long usuarioId) {
        if (responsavelIds == null || responsavelIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        return responsavelIds.stream()
                .map(id -> buscarContato(quadroId, id, usuarioId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Contato obterContatoDoUsuario(Long usuarioId, Long chaveContato) {
        return obterContatoDoUsuario(null, usuarioId, chaveContato);
    }

    private Contato obterContatoDoUsuario(QuadroPlanejamento quadro, Long usuarioId, Long chaveContato) {
        return contatoRepository.findByOwnerIdAndIdContato(usuarioId, chaveContato)
                .or(() -> contatoRepository.findByIdAndOwnerId(chaveContato, usuarioId))
                .or(() -> localizarContatoEmQuadro(quadro, chaveContato))
                .or(() -> localizarContatoDoDonoDoQuadro(quadro, chaveContato))
                .or(() -> localizarContatoDoProprioUsuario(usuarioId, chaveContato))
                .orElseThrow(() -> new ResourceNotFoundException("Contato não encontrado"));
    }

    private Optional<Contato> localizarContatoEmQuadro(QuadroPlanejamento quadro, Long chaveContato) {
        if (quadro == null || chaveContato == null) {
            return Optional.empty();
        }

        Contato contatoQuadro = quadro.getContato();
        if (isContatoCorrespondente(contatoQuadro, chaveContato)) {
            return Optional.of(contatoQuadro);
        }

        Grupo grupo = quadro.getGrupo();
        if (grupo != null) {
            List<Contato> membros = grupo.getMembros();
            if (membros != null) {
                return membros.stream()
                        .filter(Objects::nonNull)
                        .filter(contato -> isContatoCorrespondente(contato, chaveContato))
                        .findFirst();
            }
        }

        return Optional.empty();
    }

    private boolean isContatoCorrespondente(Contato contato, Long chaveContato) {
        if (contato == null || chaveContato == null) {
            return false;
        }
        if (chaveContato.equals(contato.getIdContato()) || chaveContato.equals(contato.getId())) {
            return true;
        }
        return contato.getIdContato() == null && chaveContato.equals(contato.getOwnerId());
    }

    private Optional<Contato> localizarContatoDoDonoDoQuadro(QuadroPlanejamento quadro, Long chaveContato) {
        if (quadro == null || chaveContato == null) {
            return Optional.empty();
        }

        Usuario dono = quadro.getUsuario();
        if (dono == null || dono.getId() == null || !Objects.equals(dono.getId(), chaveContato)) {
            return Optional.empty();
        }

        Contato contatoQuadro = quadro.getContato();
        if (isContatoCorrespondente(contatoQuadro, chaveContato)) {
            return Optional.of(contatoQuadro);
        }

        Grupo grupo = quadro.getGrupo();
        if (grupo != null && grupo.getMembros() != null) {
            Optional<Contato> contatoGrupo = grupo.getMembros().stream()
                    .filter(Objects::nonNull)
                    .filter(contato -> isContatoCorrespondente(contato, chaveContato))
                    .findFirst();
            if (contatoGrupo.isPresent()) {
                return contatoGrupo;
            }
        }

        return contatoRepository.findByIdContato(chaveContato).stream()
                .filter(Objects::nonNull)
                .filter(contato -> Objects.equals(contato.getOwnerId(), dono.getId()))
                .filter(contato -> isContatoCorrespondente(contato, chaveContato))
                .findFirst();
    }

    private Optional<Contato> localizarContatoDoProprioUsuario(Long usuarioId, Long chaveContato) {
        if (usuarioId == null || chaveContato == null || !Objects.equals(usuarioId, chaveContato)) {
            return Optional.empty();
        }

        Optional<Contato> contatoExistente = contatoRepository.findByOwnerIdAndIdContato(usuarioId, chaveContato);
        if (contatoExistente.isPresent()) {
            return contatoExistente;
        }

        Optional<Usuario> usuarioOptional = usuarioRepository.findById(usuarioId);
        if (usuarioOptional.isEmpty()) {
            return Optional.empty();
        }

        Usuario usuario = usuarioOptional.get();
        String email = usuario.getEmail();
        if (email != null && !email.isBlank()) {
            Optional<Contato> contatoPorEmail = contatoRepository.findByOwnerIdAndEmailIgnoreCase(usuarioId, email);
            if (contatoPorEmail.isPresent()) {
                Contato contato = contatoPorEmail.get();
                if (!Objects.equals(contato.getIdContato(), chaveContato)) {
                    contato.setIdContato(chaveContato);
                    contatoRepository.save(contato);
                }
                return Optional.of(contato);
            }
        }

        Contato novoContato = new Contato();
        novoContato.setOwnerId(usuarioId);
        novoContato.setIdContato(usuarioId);
        novoContato.setNome(usuario.getNomeUsuario());
        novoContato.setEmail(email);
        novoContato.setPendente(false);
        LocalDateTime agora = LocalDateTime.now();
        novoContato.setDataSolicitacao(agora);
        novoContato.setDataConfirmacao(agora);

        return Optional.of(contatoRepository.save(novoContato));
    }


    @Transactional(readOnly = true)
    public List<TarefaDto> getProximasTarefas(Long usuarioId) {

        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime inicio = LocalDate.now(zone).atStartOfDay();
        LocalDateTime fim = inicio.plusDays(15).with(LocalTime.MAX);

        List<TarefaPlanejamento> tarefas = tarefaRepository.findProximasTarefasPorParticipante(
                usuarioId,
                inicio,
                fim
        );

        return tarefas.stream()
                .map(tarefa -> mapEntidadeParaDto(tarefa, usuarioId))
                .collect(Collectors.toList());
    }

    private TarefaDto mapEntidadeParaDto(TarefaPlanejamento tarefa, Long usuarioId) {
        String prazo = formatDateTimeToIso(tarefa.getDataPrazo());

        String nomeQuadro = "Sem quadro";
        if (tarefa.getColuna() != null && tarefa.getColuna().getQuadro() != null) {
             // CORREÇÃO APLICADA AQUI
            nomeQuadro = tarefa.getColuna().getQuadro().getTitulo();
        }

        boolean receberNotificacoes = tarefa.getNotificacoes().stream()
                .anyMatch(notificacao ->
                        notificacao.getUsuario() != null
                                && Objects.equals(notificacao.getUsuario().getId(), usuarioId)
                );

        return new TarefaDto(
                tarefa.getTitulo(),
                prazo,
               nomeQuadro,
                receberNotificacoes
        );
    }

    
    private Set<Long> extrairParticipantes(QuadroPlanejamento quadro) {
        if (quadro == null) {
            return Collections.emptySet();
        }

        Set<Long> participantes = new LinkedHashSet<>();

        Contato contato = quadro.getContato();
        adicionarParticipante(participantes, contato);

        Grupo grupo = quadro.getGrupo();
        if (grupo != null) {
            if (grupo.getMembros() != null) {
                grupo.getMembros().forEach(membro -> adicionarParticipante(participantes, membro));
            }
        }

        Long donoQuadroId = quadro.getUsuario() != null ? quadro.getUsuario().getId() : null;
        participantes.remove(donoQuadroId);

        return participantes;
    }

    private void adicionarParticipante(Set<Long> participantes, Contato contato) {
        if (contato == null) {
            return;
        }
        Long participanteId = contato.getIdContato();
        if (participanteId != null) {
            participantes.add(participanteId);
        }
    }

    private void notificarNovosParticipantes(QuadroPlanejamento quadro, Set<Long> novosParticipantes) {
        if (quadro == null || novosParticipantes == null || novosParticipantes.isEmpty()) {
            return;
        }

        Long referenciaId = quadro.getId();
        if (referenciaId == null) {
            return;
        }

        novosParticipantes.stream()
                .filter(Objects::nonNull)
                .filter(this::desejaReceberNotificacaoDeQuadro)
                .map(usuarioRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(usuario -> registrarNotificacao(quadro, usuario));
    }

    private boolean desejaReceberNotificacaoDeQuadro(Long usuarioId) {
        if (usuarioId == null) {
            return false;
        }
        return notificacaoConfiguracaoRepository.findByUsuarioId(usuarioId)
                .map(NotificacaoConfiguracao::isIncluirEmQuadro)
                .orElse(true);
    }

    private void registrarNotificacao(QuadroPlanejamento quadro, Usuario usuario) {
        Long quadroId = quadro.getId();
        if (quadroId == null || usuario == null || usuario.getId() == null) {
            return;
        }

        Notificacao notificacao = notificacaoRepository
                .findByUsuarioIdAndTipoAndCategoriaAndReferenciaId(usuario.getId(),
                        NOTIFICACAO_TIPO, NOTIFICACAO_CATEGORIA, quadroId)
                .orElseGet(Notificacao::new);

        boolean nova = notificacao.getId() == null;
        notificacao.setUsuario(usuario);
        notificacao.setConvite(null);
        notificacao.setTitulo(NOTIFICACAO_TITULO);
        notificacao.setMensagem(montarMensagemQuadro(quadro));
        notificacao.setTipo(NOTIFICACAO_TIPO);
        notificacao.setCategoria(NOTIFICACAO_CATEGORIA);
        notificacao.setReferenciaId(quadroId);
         if (nova) {
            notificacao.setInteracaoPendente(true);
        }
        notificacao.setMetadataJson(gerarMetadataQuadro(quadro));
        LocalDateTime agora = agora();
        if (nova) {
            notificacao.setCriadaEm(agora);
            notificacao.setLida(false);
        }
        notificacao.setAtualizadaEm(agora);
        notificacaoRepository.save(notificacao);
    }

     private Set<Long> extrairResponsaveisIds(TarefaPlanejamento tarefa) {
        if (tarefa == null) {
            return Collections.emptySet();
        }

        List<Long> ids = tarefa.getResponsaveisIds();
        if (ids == null || ids.isEmpty()) {
            return Collections.emptySet();
        }

        return new LinkedHashSet<>(ids);
    }

    private void notificarResponsaveisAtribuidos(TarefaPlanejamento tarefa, Set<Long> novosResponsaveis, Long autorId) {
        if (tarefa == null || tarefa.getId() == null || novosResponsaveis == null || novosResponsaveis.isEmpty()) {
            return;
        }

        novosResponsaveis.stream()
                .filter(Objects::nonNull)
                .filter(responsavelId -> !Objects.equals(responsavelId, autorId))
                .filter(this::desejaReceberNotificacaoDePrazo)
                .map(usuarioRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(usuario -> registrarNotificacaoTarefaAtribuida(tarefa, usuario));
    }

    private boolean desejaReceberNotificacaoDePrazo(Long usuarioId) {
        if (usuarioId == null) {
            return false;
        }
        return notificacaoConfiguracaoRepository.findByUsuarioId(usuarioId)
                .map(NotificacaoConfiguracao::isPrazoTarefa)
                .orElse(true);
    }

    private void sincronizarInscricoesComentarios(TarefaPlanejamento tarefa, Set<Long> responsaveis) {
        if (tarefa == null || tarefa.getId() == null || responsaveis == null || responsaveis.isEmpty()) {
            return;
        }

        responsaveis.stream()
                .filter(Objects::nonNull)
                .forEach(responsavelId -> sincronizarInscricaoComentario(tarefa, responsavelId));
    }

    private void sincronizarInscricaoComentario(TarefaPlanejamento tarefa, Long usuarioId) {
        if (usuarioId == null || tarefa == null || tarefa.getId() == null) {
            return;
        }

        boolean desejaReceber = notificacaoConfiguracaoRepository.findByUsuarioId(usuarioId)
                .map(NotificacaoConfiguracao::isComentarioTarefa)
                .orElse(true);
        boolean inscrito = tarefaNotificacaoRepository.existsByTarefaIdAndUsuarioId(tarefa.getId(), usuarioId);

        if (desejaReceber && !inscrito) {
            usuarioRepository.findById(usuarioId)
                    .ifPresent(usuario -> {
                        TarefaNotificacao notificacao = new TarefaNotificacao();
                        notificacao.setTarefa(tarefa);
                        notificacao.setUsuario(usuario);
                        tarefaNotificacaoRepository.save(notificacao);
                    });
        } else if (!desejaReceber && inscrito) {
            tarefaNotificacaoRepository.deleteByTarefaIdAndUsuarioId(tarefa.getId(), usuarioId);
        }
    }

    private void removerInscricoesComentarios(TarefaPlanejamento tarefa, Set<Long> removidos) {
        if (tarefa == null || tarefa.getId() == null || removidos == null || removidos.isEmpty()) {
            return;
        }
        removidos.stream()
                .filter(Objects::nonNull)
                .forEach(usuarioId -> removerInscricaoComentario(tarefa, usuarioId));
    }

    private void removerInscricaoComentario(TarefaPlanejamento tarefa, Long usuarioId) {
        if (usuarioId == null || tarefa == null || tarefa.getId() == null) {
            return;
        }
        tarefaNotificacaoRepository.deleteByTarefaIdAndUsuarioId(tarefa.getId(), usuarioId);
    }

    private void registrarNotificacaoTarefaAtribuida(TarefaPlanejamento tarefa, Usuario usuario) {
        if (usuario == null || usuario.getId() == null || tarefa == null || tarefa.getId() == null) {
            return;
        }

        Notificacao notificacao = notificacaoRepository
                .findByUsuarioIdAndTipoAndCategoriaAndReferenciaId(usuario.getId(),
                        NOTIFICACAO_TIPO, NOTIFICACAO_TAREFA_ATRIBUIDA_CATEGORIA, tarefa.getId())
                .orElseGet(Notificacao::new);

        boolean nova = notificacao.getId() == null;
        notificacao.setUsuario(usuario);
        notificacao.setConvite(null);
        notificacao.setTitulo(NOTIFICACAO_TAREFA_ATRIBUIDA_TITULO);
        notificacao.setMensagem(montarMensagemTarefaAtribuida(tarefa));
        notificacao.setTipo(NOTIFICACAO_TIPO);
        notificacao.setCategoria(NOTIFICACAO_TAREFA_ATRIBUIDA_CATEGORIA);
        notificacao.setReferenciaId(tarefa.getId());
        if (nova) {
            notificacao.setInteracaoPendente(true);
        }
        notificacao.setMetadataJson(gerarMetadataTarefa(tarefa));
        LocalDateTime agora = agora();
        if (nova) {
            notificacao.setCriadaEm(agora);
            notificacao.setLida(false);
        }
        notificacao.setAtualizadaEm(agora);
        notificacaoRepository.save(notificacao);
    }

    private void notificarResponsaveisSobreComentario(TarefaPlanejamento tarefa, TarefaComentario comentario) {
        if (tarefa == null || tarefa.getId() == null || comentario == null || comentario.getId() == null) {
            return;
        }

        List<TarefaNotificacao> inscricoes = tarefaNotificacaoRepository.findByTarefaId(tarefa.getId());
        if (inscricoes == null || inscricoes.isEmpty()) {
            return;
        }

        Long autorId = comentario.getAutor() != null ? comentario.getAutor().getId() : null;
        Set<Long> processados = new LinkedHashSet<>();

        for (TarefaNotificacao inscricao : inscricoes) {
            if (inscricao == null || inscricao.getUsuario() == null) {
                continue;
            }

            Usuario destinatario = inscricao.getUsuario();
            Long destinatarioId = destinatario.getId();
            if (destinatarioId == null || Objects.equals(destinatarioId, autorId)) {
                continue;
            }
            if (!processados.add(destinatarioId)) {
                continue;
            }
            if (!desejaReceberComentarioTarefa(destinatarioId)) {
                continue;
            }
            registrarNotificacaoComentario(tarefa, comentario, destinatario);
        }
    }

    private boolean desejaReceberComentarioTarefa(Long usuarioId) {
        if (usuarioId == null) {
            return false;
        }
        return notificacaoConfiguracaoRepository.findByUsuarioId(usuarioId)
                .map(NotificacaoConfiguracao::isComentarioTarefa)
                .orElse(true);
    }

    private void registrarNotificacaoComentario(TarefaPlanejamento tarefa, TarefaComentario comentario, Usuario destinatario) {
        if (destinatario == null || destinatario.getId() == null || comentario == null || comentario.getId() == null) {
            return;
        }

        Notificacao notificacao = notificacaoRepository
                .findByUsuarioIdAndTipoAndCategoriaAndReferenciaId(destinatario.getId(),
                        NOTIFICACAO_TIPO, NOTIFICACAO_TAREFA_COMENTARIO_CATEGORIA, comentario.getId())
                .orElseGet(Notificacao::new);

        boolean nova = notificacao.getId() == null;
        notificacao.setUsuario(destinatario);
        notificacao.setConvite(null);
        notificacao.setTitulo(NOTIFICACAO_TAREFA_COMENTARIO_TITULO);
        notificacao.setMensagem(montarMensagemComentario(tarefa, comentario.getAutor()));
        notificacao.setTipo(NOTIFICACAO_TIPO);
        notificacao.setCategoria(NOTIFICACAO_TAREFA_COMENTARIO_CATEGORIA);
        notificacao.setReferenciaId(comentario.getId());
         if (nova) {
            notificacao.setInteracaoPendente(true);
        }
        notificacao.setMetadataJson(gerarMetadataComentario(tarefa, comentario));
        LocalDateTime agora = agora();
        if (nova) {
            notificacao.setCriadaEm(agora);
            notificacao.setLida(false);
        }
        notificacao.setAtualizadaEm(agora);
        notificacaoRepository.save(notificacao);
    }

    private String montarMensagemTarefaAtribuida(TarefaPlanejamento tarefa) {
        if (tarefa == null) {
            return "Você foi atribuído a uma tarefa.";
        }
        String tituloTarefa = tarefa.getTitulo();
        QuadroPlanejamento quadro = obterQuadroDaTarefa(tarefa);
        String tituloQuadro = quadro != null ? quadro.getTitulo() : null;

        if (tituloTarefa != null && !tituloTarefa.isBlank()) {
            if (tituloQuadro != null && !tituloQuadro.isBlank()) {
                return String.format("Você foi atribuído à tarefa \"%s\" no quadro \"%s\".", tituloTarefa, tituloQuadro);
            }
            return String.format("Você foi atribuído à tarefa \"%s\".", tituloTarefa);
        }

        if (tituloQuadro != null && !tituloQuadro.isBlank()) {
            return String.format("Você foi atribuído a uma tarefa no quadro \"%s\".", tituloQuadro);
        }

        return "Você foi atribuído a uma tarefa.";
    }

    private String gerarMetadataTarefa(TarefaPlanejamento tarefa) {
        if (objectMapper == null || tarefa == null || tarefa.getId() == null) {
            return null;
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("action", "OPEN_TAREFA");
        metadata.put("tarefaId", tarefa.getId());

        QuadroPlanejamento quadro = obterQuadroDaTarefa(tarefa);
        if (quadro != null && quadro.getId() != null) {
            metadata.put("quadroId", quadro.getId());
        }

        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String montarMensagemComentario(TarefaPlanejamento tarefa, Usuario autor) {
        String nomeAutor = autor != null && autor.getNomeUsuario() != null && !autor.getNomeUsuario().isBlank()
                ? autor.getNomeUsuario()
                : "Alguém";

        String tituloTarefa = tarefa != null ? tarefa.getTitulo() : null;
        if (tituloTarefa != null && !tituloTarefa.isBlank()) {
            return String.format("%s comentou na tarefa \"%s\".", nomeAutor, tituloTarefa);
        }
        return String.format("%s comentou em uma tarefa.", nomeAutor);
    }

    private String gerarMetadataComentario(TarefaPlanejamento tarefa, TarefaComentario comentario) {
        if (objectMapper == null || comentario == null || comentario.getId() == null) {
            return null;
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("action", "OPEN_TAREFA_COMENTARIO");
        metadata.put("comentarioId", comentario.getId());
        if (tarefa != null && tarefa.getId() != null) {
            metadata.put("tarefaId", tarefa.getId());
        }
        QuadroPlanejamento quadro = obterQuadroDaTarefa(tarefa);
        if (quadro != null && quadro.getId() != null) {
            metadata.put("quadroId", quadro.getId());
        }

        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private QuadroPlanejamento obterQuadroDaTarefa(TarefaPlanejamento tarefa) {
        if (tarefa == null || tarefa.getColuna() == null) {
            return null;
        }
        return tarefa.getColuna().getQuadro();
    }

    private String montarMensagemQuadro(QuadroPlanejamento quadro) {
        String titulo = quadro.getTitulo();
        if (titulo == null || titulo.isBlank()) {
            return "Você foi adicionado a um quadro.";
        }
        return String.format("Você foi adicionado ao quadro \"%s\".", titulo);
    }

    private String gerarMetadataQuadro(QuadroPlanejamento quadro) {
        if (objectMapper == null || quadro == null || quadro.getId() == null) {
            return null;
        }

        Map<String, Object> metadata = Map.of(
                "action", "OPEN_QUADRO",
                "quadroId", quadro.getId()
        );
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(ZONA_BRASIL);
    }
}