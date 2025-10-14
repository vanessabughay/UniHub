package com.unihub.backend.service;

import com.unihub.backend.dto.planejamento.*;
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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
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

    public List<QuadroPlanejamentoListaResponse> listar(Long usuarioId, QuadroStatus status, String titulo) {
        boolean possuiTitulo = titulo != null && !titulo.trim().isEmpty();
        List<QuadroPlanejamento> quadros;

        if (status != null && possuiTitulo) {
            quadros = repository.findByUsuarioIdAndStatusAndTituloContainingIgnoreCase(usuarioId, status, titulo);
        } else if (status != null) {
            quadros = repository.findByUsuarioIdAndStatus(usuarioId, status);
        } else if (possuiTitulo) {
            quadros = repository.findByUsuarioIdAndTituloContainingIgnoreCase(usuarioId, titulo);
        } else {
            quadros = repository.findByUsuarioId(usuarioId);
        }

        return quadros.stream()
                .map(QuadroPlanejamentoListaResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public QuadroPlanejamento buscarPorId(Long id, Long usuarioId) {
        return repository.findByIdAndUsuarioId(id, usuarioId)
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
            Contato contato = contatoRepository.findById(dto.getContatoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contato não encontrado"));
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
            Contato contato = contatoRepository.findById(dto.getContatoId()).orElseThrow(() -> new ResourceNotFoundException("Contato não encontrado"));
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
            tarefa.setDataPrazo(convertEpochToLocalDate(request.getPrazo()));
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
        response.setPrazo(convertLocalDateToEpoch(tarefa.getDataPrazo()));
        response.setDataInicio(convertLocalDateTimeToEpoch(tarefa.getDataCriacao()));
        response.setDataFim(convertLocalDateTimeToEpoch(tarefa.getDataConclusao()));
                response.setResponsavelIds(tarefa.getResponsaveisIds());
        return response;
    }

    private Long convertLocalDateToEpoch(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private Long convertLocalDateTimeToEpoch(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private LocalDate convertEpochToLocalDate(Long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalDateTime convertEpochToLocalDateTime(Long epochMillis, LocalDateTime defaultValue) {
        if (epochMillis == null) {
            return defaultValue;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
    }

    private Contato buscarContato(Long quadroId, Long contatoId, Long usuarioId) {
        Contato contato = contatoRepository.findByIdAndOwnerId(contatoId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Contato não encontrado"));

        buscarPorId(quadroId, usuarioId);

        return contato;
    }

    private LinkedHashSet<Contato> buscarResponsaveis(Long quadroId, List<Long> responsavelIds, Long usuarioId) {
        if (responsavelIds == null || responsavelIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        return responsavelIds.stream()
                .map(id -> buscarContato(quadroId, id, usuarioId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}