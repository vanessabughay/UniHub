package com.unihub.backend.service;

import com.unihub.backend.dto.planejamento.AdicionarGruposRequest;
import com.unihub.backend.dto.planejamento.AdicionarMembrosRequest;
import com.unihub.backend.dto.planejamento.AtualizarStatusTarefaRequest;
import com.unihub.backend.dto.planejamento.ColunaPlanejamentoRequest;
import com.unihub.backend.dto.planejamento.QuadroPlanejamentoDetalhesResponse;
import com.unihub.backend.dto.planejamento.QuadroPlanejamentoListaResponse;
import com.unihub.backend.dto.planejamento.TarefaPlanejamentoRequest;
import com.unihub.backend.exceptions.ResourceNotFoundException;
import com.unihub.backend.model.ColunaPlanejamento;
import com.unihub.backend.model.Contato;
import com.unihub.backend.model.Grupo;
import com.unihub.backend.model.QuadroPlanejamento;
import com.unihub.backend.model.TarefaPlanejamento;
import com.unihub.backend.model.Usuario;
import com.unihub.backend.model.enums.EstadoPlanejamento;
import com.unihub.backend.model.enums.QuadroStatus;
import com.unihub.backend.model.enums.TarefaStatus;
import com.unihub.backend.repository.ColunaPlanejamentoRepository;
import com.unihub.backend.repository.ContatoRepository;
import com.unihub.backend.repository.GrupoRepository;
import com.unihub.backend.repository.QuadroPlanejamentoRepository;
import com.unihub.backend.repository.TarefaPlanejamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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

    @Transactional
    public QuadroPlanejamento criar(QuadroPlanejamento quadro, Long usuarioId) {
        quadro.setId(null);
if (quadro.getDataCriacao() == null) {
            quadro.setDataCriacao(Instant.now());
        }
                quadro.setUsuario(referenciaUsuario(usuarioId));
        
        ajustarStatus(quadro, null);
        return repository.save(quadro);
    }

    @Transactional
    public QuadroPlanejamento atualizar(Long id, QuadroPlanejamento quadroAtualizado, Long usuarioId) {
        QuadroPlanejamento existente = buscarPorId(id, usuarioId);
        QuadroStatus statusAnterior = existente.getStatus();

        existente.setTitulo(quadroAtualizado.getTitulo());
        existente.setDataPrazo(quadroAtualizado.getDataPrazo());
        existente.setDisciplina(quadroAtualizado.getDisciplina());
        existente.setIntegrantes(quadroAtualizado.getIntegrantes());

        if (quadroAtualizado.getStatus() != null) {
            existente.setStatus(quadroAtualizado.getStatus());
        }

        ajustarStatus(existente, statusAnterior);

        return repository.save(existente);
    }

     @Transactional
    public void excluir(Long id, Long usuarioId) {
        QuadroPlanejamento quadro = buscarPorId(id, usuarioId);
        repository.delete(quadro);
    }

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
        response.setDisciplina(quadro.getDisciplina());
        response.setIntegrantes(quadro.getIntegrantes());
        response.setDonoId(quadro.getDonoId());
        response.setMembros(quadro.getMembros());
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
        coluna.setOrdem(request.getOrdem());
        coluna.setQuadro(quadro);

        return colunaRepository.save(coluna);
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

    @Transactional
    public QuadroPlanejamento adicionarMembros(Long quadroId, AdicionarMembrosRequest request, Long usuarioId) {
        QuadroPlanejamento quadro = buscarPorId(quadroId, usuarioId);
        List<Long> ids = Optional.ofNullable(request.getContatosIds()).orElse(Collections.emptyList());
        for (Long contatoId : ids) {
            Contato contato = contatoRepository.findByIdAndOwnerId(contatoId, usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Contato não encontrado"));
            quadro.getMembros().add(contato);
        }
        return repository.save(quadro);
    }

    @Transactional
    public QuadroPlanejamento adicionarGrupos(Long quadroId, AdicionarGruposRequest request, Long usuarioId) {
        QuadroPlanejamento quadro = buscarPorId(quadroId, usuarioId);
        List<Long> ids = Optional.ofNullable(request.getGruposIds()).orElse(Collections.emptyList());

        for (Long grupoId : ids) {
            Grupo grupo = grupoRepository.findById(grupoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado"));
            for (Contato membro : grupo.getMembros()) {
                if (Objects.equals(membro.getOwnerId(), usuarioId)) {
                    quadro.getMembros().add(membro);
                }
            }
        }

        return repository.save(quadro);
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
        Set<Contato> membros = quadro.getMembros();
        if (!membros.contains(contato)) {
            throw new IllegalArgumentException("Contato não é membro deste quadro");
        }
        return contato;
    }
}