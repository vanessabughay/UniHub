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
import java.time.LocalDateTime;
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
        List<ColunaPlanejamento> colunas = colunaRepository.findByQuadroIdOrderByDescricaoAsc(quadro.getId());

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
        coluna.setDescricao(request.getDescricao());
        coluna.setQuadro(quadro);

        return colunaRepository.save(coluna);
    }

    @Transactional(readOnly = true)
    public List<ColunaPlanejamento> listarColunas(Long quadroId, EstadoPlanejamento estado, Long usuarioId) {
        buscarPorId(quadroId, usuarioId);
        if (estado == null) {
            return colunaRepository.findByQuadroIdOrderByDescricaoAsc(quadroId);
                }
        return colunaRepository.findByQuadroIdAndEstadoOrderByDescricaoAsc(quadroId, estado);
        }

    @Transactional(readOnly = true)
    public List<TarefaPlanejamento> listarTarefas(Long quadroId, Long colunaId, Long usuarioId) {
        ColunaPlanejamento coluna = buscarColuna(quadroId, colunaId, usuarioId);
        return tarefaRepository.findByColunaIdOrderByDataPrazoAsc(coluna.getId());
    }

     @Transactional(readOnly = true)
    public ColunaPlanejamento buscarColunaPorId(Long quadroId, Long colunaId, Long usuarioId) {
        return buscarColuna(quadroId, colunaId, usuarioId);
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

        if (request.getResponsavelId() != null) {
            Contato contato = buscarContato(quadroId, request.getResponsavelId(), usuarioId);
            tarefa.setResponsavel(contato);
        }

        return tarefaRepository.save(tarefa);
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

    private Contato buscarContato(Long quadroId, Long contatoId, Long usuarioId) {
        Contato contato = contatoRepository.findByIdAndOwnerId(contatoId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Contato não encontrado"));

        QuadroPlanejamento quadro = buscarPorId(quadroId, usuarioId);
      
        return contato;
    }
}