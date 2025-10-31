package com.unihub.backend.controller;

//  novo DTO
import com.unihub.backend.dto.planejamento.QuadroPlanejamentoRequest;

import com.unihub.backend.dto.planejamento.TarefaDto; 
import com.unihub.backend.dto.planejamento.AtualizarPreferenciaTarefaRequest;
import com.unihub.backend.dto.planejamento.AtualizarStatusTarefaRequest;
import com.unihub.backend.dto.planejamento.AtualizarTarefaPlanejamentoRequest;
import com.unihub.backend.dto.planejamento.ColunaPlanejamentoRequest;
import com.unihub.backend.dto.planejamento.QuadroPlanejamentoDetalhesResponse;
import com.unihub.backend.dto.planejamento.QuadroPlanejamentoListaResponse;
import com.unihub.backend.dto.planejamento.TarefaComentarioRequest;
import com.unihub.backend.dto.planejamento.TarefaComentarioResponse;
import com.unihub.backend.dto.planejamento.TarefaComentariosResponse;
import com.unihub.backend.dto.planejamento.TarefaPlanejamentoRequest;
import com.unihub.backend.dto.planejamento.PreferenciaTarefaResponse;
import com.unihub.backend.dto.planejamento.TarefaPlanejamentoResponse;
import com.unihub.backend.model.ColunaPlanejamento;
import com.unihub.backend.model.QuadroPlanejamento;
import com.unihub.backend.model.TarefaPlanejamento;
import com.unihub.backend.model.enums.EstadoPlanejamento;
import com.unihub.backend.model.enums.QuadroStatus;
import com.unihub.backend.service.QuadroPlanejamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/quadros-planejamento")
@CrossOrigin(origins = "*")
public class QuadroPlanejamentoController {

    @Autowired
    private QuadroPlanejamentoService service;

    @GetMapping
    public List<QuadroPlanejamentoListaResponse> listar(@AuthenticationPrincipal Long usuarioId,
                                                        @RequestParam(value = "status", required = false) QuadroStatus status,
                                                        @RequestParam(value = "titulo", required = false) String titulo) {
        return service.listar(usuarioId, status, titulo);
    }

    @GetMapping("/{id}")
    public QuadroPlanejamento buscarPorId(@PathVariable Long id, @AuthenticationPrincipal Long usuarioId) {
        return service.buscarPorId(id, usuarioId);
    }

    @GetMapping("/{id}/detalhes")
    public QuadroPlanejamentoDetalhesResponse detalhes(@PathVariable Long id,
                                                       @AuthenticationPrincipal Long usuarioId) {
        return service.detalhes(id, usuarioId);
    }

    @GetMapping("/{id}/colunas")
    public List<ColunaPlanejamento> listarColunas(@PathVariable Long id,
                                                   @RequestParam(value = "estado", required = false) EstadoPlanejamento estado,
                                                   @AuthenticationPrincipal Long usuarioId) {
        return service.listarColunas(id, estado, usuarioId);
    }

    @GetMapping("/{id}/colunas/{colunaId}")
    public ColunaPlanejamento buscarColuna(@PathVariable Long id,
                                           @PathVariable Long colunaId,
                                           @AuthenticationPrincipal Long usuarioId) {
        return service.buscarColunaPorId(id, colunaId, usuarioId);
    }


    @PostMapping("/{id}/colunas")
    public ColunaPlanejamento criarColuna(@PathVariable Long id,
                                           @RequestBody ColunaPlanejamentoRequest request,
                                           @AuthenticationPrincipal Long usuarioId) {
        return service.criarColuna(id, request, usuarioId);
    }

    @PutMapping("/{id}/colunas/{colunaId}")
    public ColunaPlanejamento atualizarColuna(@PathVariable Long id,
                                              @PathVariable Long colunaId,
                                              @RequestBody ColunaPlanejamentoRequest request,
                                              @AuthenticationPrincipal Long usuarioId) {
        return service.atualizarColuna(id, colunaId, request, usuarioId);
    }

    @DeleteMapping("/{id}/colunas/{colunaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirColuna(@PathVariable Long id,
                              @PathVariable Long colunaId,
                              @AuthenticationPrincipal Long usuarioId) {
        service.excluirColuna(id, colunaId, usuarioId);
    }

    @GetMapping("/{id}/colunas/{colunaId}/tarefas")
    public List<TarefaPlanejamento> listarTarefas(@PathVariable Long id,
                                                  @PathVariable Long colunaId,
                                                  @AuthenticationPrincipal Long usuarioId) {
        return service.listarTarefas(id, colunaId, usuarioId);
    }

    //
    @GetMapping("/tarefas/proximas")
    public ResponseEntity<List<TarefaDto>> getProximasTarefas(@AuthenticationPrincipal Long usuarioId) {
        List<TarefaDto> tarefas = service.getProximasTarefas(usuarioId);
        return ResponseEntity.ok(tarefas);
    }

    @GetMapping("/{id}/colunas/{colunaId}/tarefas/{tarefaId}")
    public TarefaPlanejamentoResponse buscarTarefa(@PathVariable Long id,
                                                   @PathVariable Long colunaId,
                                                   @PathVariable Long tarefaId,
                                                   @AuthenticationPrincipal Long usuarioId) {
        return service.buscarTarefa(id, colunaId, tarefaId, usuarioId);
    }

    @PostMapping("/{id}/colunas/{colunaId}/tarefas")
    public TarefaPlanejamento criarTarefa(@PathVariable Long id,
                                          @PathVariable Long colunaId,
                                          @RequestBody TarefaPlanejamentoRequest request,
                                          @AuthenticationPrincipal Long usuarioId) {
        return service.criarTarefa(id, colunaId, request, usuarioId);
    }
    @PutMapping("/{id}/colunas/{colunaId}/tarefas/{tarefaId}")
    public TarefaPlanejamentoResponse atualizarTarefa(@PathVariable Long id,
                                                      @PathVariable Long colunaId,
                                                      @PathVariable Long tarefaId,
                                                      @RequestBody AtualizarTarefaPlanejamentoRequest request,
                                                      @AuthenticationPrincipal Long usuarioId) {
        return service.atualizarTarefa(id, colunaId, tarefaId, request, usuarioId);
    }

    @DeleteMapping("/{id}/colunas/{colunaId}/tarefas/{tarefaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirTarefa(@PathVariable Long id,
                              @PathVariable Long colunaId,
                              @PathVariable Long tarefaId,
                              @AuthenticationPrincipal Long usuarioId) {
        service.excluirTarefa(id, colunaId, tarefaId, usuarioId);
    }

    @GetMapping("/{id}/colunas/{colunaId}/tarefas/{tarefaId}/comentarios")
    public TarefaComentariosResponse listarComentarios(@PathVariable Long id,
                                                       @PathVariable Long colunaId,
                                                       @PathVariable Long tarefaId,
                                                       @AuthenticationPrincipal Long usuarioId) {
        return service.listarComentarios(id, colunaId, tarefaId, usuarioId);
    }

    @PostMapping("/{id}/colunas/{colunaId}/tarefas/{tarefaId}/comentarios")
    public TarefaComentarioResponse adicionarComentario(@PathVariable Long id,
                                                        @PathVariable Long colunaId,
                                                        @PathVariable Long tarefaId,
                                                        @RequestBody TarefaComentarioRequest request,
                                                        @AuthenticationPrincipal Long usuarioId) {
        return service.adicionarComentario(id, colunaId, tarefaId, request, usuarioId);
    }

    @PutMapping("/{id}/colunas/{colunaId}/tarefas/{tarefaId}/comentarios/{comentarioId}")
    public TarefaComentarioResponse atualizarComentario(@PathVariable Long id,
                                                        @PathVariable Long colunaId,
                                                        @PathVariable Long tarefaId,
                                                        @PathVariable Long comentarioId,
                                                        @RequestBody TarefaComentarioRequest request,
                                                        @AuthenticationPrincipal Long usuarioId) {
        return service.atualizarComentario(id, colunaId, tarefaId, comentarioId, request, usuarioId);
    }

    @DeleteMapping("/{id}/colunas/{colunaId}/tarefas/{tarefaId}/comentarios/{comentarioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirComentario(@PathVariable Long id,
                                  @PathVariable Long colunaId,
                                  @PathVariable Long tarefaId,
                                  @PathVariable Long comentarioId,
                                  @AuthenticationPrincipal Long usuarioId) {
        service.excluirComentario(id, colunaId, tarefaId, comentarioId, usuarioId);
    }

    @PutMapping("/{id}/colunas/{colunaId}/tarefas/{tarefaId}/preferencias")
    public PreferenciaTarefaResponse atualizarPreferenciaTarefa(@PathVariable Long id,
                                                                @PathVariable Long colunaId,
                                                                @PathVariable Long tarefaId,
                                                                @RequestBody AtualizarPreferenciaTarefaRequest request,
                                                                @AuthenticationPrincipal Long usuarioId) {
        return service.atualizarPreferenciaTarefa(id, colunaId, tarefaId, request, usuarioId);
    }

    @PatchMapping("/{id}/tarefas/{tarefaId}/status")
    public TarefaPlanejamento atualizarStatusTarefa(@PathVariable Long id,
                                                    @PathVariable Long tarefaId,
                                                    @RequestBody AtualizarStatusTarefaRequest request,
                                                    @AuthenticationPrincipal Long usuarioId) {
        return service.atualizarStatusTarefa(id, tarefaId, request, usuarioId);
    }

    // atualizando metodos com o dto
    @PostMapping
    public QuadroPlanejamento criar(
            @RequestBody QuadroPlanejamentoRequest request, // Mudou de QuadroPlanejamento para o DTO
            @AuthenticationPrincipal Long usuarioId) {
        return service.criar(request, usuarioId);
    }

    @PutMapping("/{id}")
    public QuadroPlanejamento atualizar(
            @PathVariable Long id,
            @RequestBody QuadroPlanejamentoRequest request, // Mudou de QuadroPlanejamento para o DTO
            @AuthenticationPrincipal Long usuarioId) {
        return service.atualizar(id, request, usuarioId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, @AuthenticationPrincipal Long usuarioId) {
        service.excluir(id, usuarioId);
    }

}