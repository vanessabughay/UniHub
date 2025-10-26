package com.unihub.backend.service;

import com.unihub.backend.dto.planejamento.AtualizarPreferenciaComentarioRequest;
import com.unihub.backend.dto.planejamento.AtualizarStatusTarefaRequest;
import com.unihub.backend.dto.planejamento.AtualizarTarefaPlanejamentoRequest;
import com.unihub.backend.dto.planejamento.ColunaPlanejamentoRequest;
import com.unihub.backend.dto.planejamento.PreferenciaComentarioResponse;
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

import com.unihub.backend.dto.planejamento.TarefaDto; 
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
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
    private TarefaComentarioNotificacaoRepository tarefaComentarioNotificacaoRepository;

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

        quadro.setDataCriacao(Instant.now());
        if (dto.getDataFim() != null) {
            quadro.setDataPrazo(Instant.ofEpochMilli(dto.getDataFim()));
        }
        ajustarStatus(quadro, null); 
        
        return repository.save(quadro);
    }

    // atualizar atualizado
    @Transactional
    public QuadroPlanejamento atualizar(Long id, QuadroPlanejamentoRequest dto, Long usuarioId) {
        QuadroPlanejamento existente = buscarPorId(id, usuarioId);
        QuadroStatus statusAnterior = existente.getStatus();
        
        // Validação da regra de negócio
        if (dto.getContatoId() != null && dto.getGrupoId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um quadro só pode ter um contato ou um grupo como integrante, não ambos.");
        }

        existente.setTitulo(dto.getNome());
        if (dto.getDataFim() != null) {
            existente.setDataPrazo(Instant.ofEpochMilli(dto.getDataFim()));
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

        return repository.save(existente);
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
        response.setDataInicio(quadro.getDataCriacao());
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
        tarefa.setDataPrazo(request.getDataPrazo());
        tarefa.setColuna(coluna);

        tarefa.setResponsaveis(buscarResponsaveis(quadroId, request.getResponsavelIds(), usuarioId));

        return tarefaRepository.save(tarefa);
    }

    @Transactional
    public TarefaPlanejamentoResponse atualizarTarefa(Long quadroId, Long colunaId, Long tarefaId,
                                                      AtualizarTarefaPlanejamentoRequest request, Long usuarioId) {
        TarefaPlanejamento tarefa = buscarTarefaEntity(quadroId, colunaId, tarefaId, usuarioId);

        if (request.getTitulo() != null) {
            validarTitulo(request.getTitulo());
            tarefa.setTitulo(request.getTitulo());
        }

        tarefa.setDescricao(request.getDescricao());

        if (request.getPrazo() != null) {
            tarefa.setDataPrazo(convertEpochToLocalDateTime(request.getPrazo()));
                } else {
            tarefa.setDataPrazo(null);
        }

        if ("CONCLUIDA".equalsIgnoreCase(request.getStatus())) {
            tarefa.setStatus(TarefaStatus.CONCLUIDA);
            tarefa.setDataConclusao(convertEpochToLocalDateTime(request.getDataFim(), LocalDateTime.now()));
        } else {
            tarefa.setStatus(TarefaStatus.PENDENTE);
            tarefa.setDataConclusao(null);
        }

        if (request.getResponsavelIds() != null) {
            tarefa.setResponsaveis(buscarResponsaveis(quadroId, request.getResponsavelIds(), usuarioId));
        }

        TarefaPlanejamento atualizado = tarefaRepository.save(tarefa);
        return toResponse(atualizado);
    }

    @Transactional
    public TarefaPlanejamento atualizarStatusTarefa(Long quadroId, Long tarefaId, AtualizarStatusTarefaRequest request, Long usuarioId) {
        TarefaPlanejamento tarefa = tarefaRepository.findByIdAndColunaQuadroId(tarefaId, quadroId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada"));

        buscarPorId(quadroId, usuarioId);

        if (request.isConcluida()) {
            tarefa.setStatus(TarefaStatus.CONCLUIDA);
            tarefa.setDataConclusao(LocalDateTime.now());
        } else {
            tarefa.setStatus(TarefaStatus.PENDENTE);
            tarefa.setDataConclusao(null);
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

        boolean receberNotificacoes = tarefaComentarioNotificacaoRepository
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
    public PreferenciaComentarioResponse atualizarPreferenciaComentario(Long quadroId, Long colunaId, Long tarefaId,
                                                                        AtualizarPreferenciaComentarioRequest request,
                                                                        Long usuarioId) {
        TarefaPlanejamento tarefa = buscarTarefaEntity(quadroId, colunaId, tarefaId, usuarioId);

        boolean desejaReceber = request.isReceberNotificacoes();
        if (desejaReceber) {
            boolean jaExiste = tarefaComentarioNotificacaoRepository
                    .existsByTarefaIdAndUsuarioId(tarefa.getId(), usuarioId);
            if (!jaExiste) {
                Usuario usuario = usuarioRepository.findById(usuarioId)
                        .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
                TarefaComentarioNotificacao notificacao = new TarefaComentarioNotificacao();
                notificacao.setTarefa(tarefa);
                notificacao.setUsuario(usuario);
                tarefaComentarioNotificacaoRepository.save(notificacao);
            }
        } else {
            tarefaComentarioNotificacaoRepository.deleteByTarefaIdAndUsuarioId(tarefa.getId(), usuarioId);
        }

        boolean estadoAtual = tarefaComentarioNotificacaoRepository
                .existsByTarefaIdAndUsuarioId(tarefa.getId(), usuarioId);
        return new PreferenciaComentarioResponse(estadoAtual);
    }


    private Usuario referenciaUsuario(Long usuarioId) {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        return usuario;
    }

    private void ajustarStatus(QuadroPlanejamento quadro, QuadroStatus statusAnterior) {
        if (quadro.getStatus() == null) {
            quadro.setStatus(QuadroStatus.ATIVO);
        }

        if (quadro.getStatus() == QuadroStatus.ENCERRADO && quadro.getDataPrazo() == null) {
            quadro.setDataPrazo(Instant.now());
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
        response.setPrazo(convertLocalDateTimeToEpoch(tarefa.getDataPrazo()));
        response.setDataInicio(convertLocalDateTimeToEpoch(tarefa.getDataCriacao()));
        response.setDataFim(convertLocalDateTimeToEpoch(tarefa.getDataConclusao()));
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


    private Long convertLocalDateTimeToEpoch(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private LocalDateTime convertEpochToLocalDateTime(Long epochMillis) {
        if (epochMillis == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
    }

    private LocalDateTime convertEpochToLocalDateTime(Long epochMillis, LocalDateTime defaultValue) {
        LocalDateTime converted = convertEpochToLocalDateTime(epochMillis);
        return converted != null ? converted : defaultValue;
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

        LocalDateTime dataInicio = LocalDate.now().atStartOfDay();
        LocalDateTime dataFim = dataInicio.plusDays(15).with(LocalTime.MAX);

        List<TarefaPlanejamento> tarefas = tarefaRepository.findProximasTarefasPorResponsavel(
                usuarioId, 
                dataInicio,
                dataFim
        );

        return tarefas.stream()
                .map(this::mapEntidadeParaDto)
                .collect(Collectors.toList());
    }

    private TarefaDto mapEntidadeParaDto(TarefaPlanejamento tarefa) {
        String dataPrazoFormatada = null;
        if (tarefa.getDataPrazo() != null) {
            dataPrazoFormatada = tarefa.getDataPrazo().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        String nomeQuadro = "Sem quadro";
        if (tarefa.getColuna() != null && tarefa.getColuna().getQuadro() != null) {
             // CORREÇÃO APLICADA AQUI
            nomeQuadro = tarefa.getColuna().getQuadro().getTitulo();
        }

        return new TarefaDto(
                tarefa.getTitulo(),
                dataPrazoFormatada,
                nomeQuadro
        );
    }
}