package com.unihub.backend.controller;

//  novo DTO
import com.unihub.backend.dto.planejamento.QuadroPlanejamentoRequest;

import com.unihub.backend.dto.planejamento.AdicionarGruposRequest;
import com.unihub.backend.dto.planejamento.AdicionarMembrosRequest;
import com.unihub.backend.dto.planejamento.AtualizarStatusTarefaRequest;
import com.unihub.backend.dto.planejamento.ColunaPlanejamentoRequest;
import com.unihub.backend.dto.planejamento.QuadroPlanejamentoDetalhesResponse;
import com.unihub.backend.dto.planejamento.QuadroPlanejamentoListaResponse;
import com.unihub.backend.dto.planejamento.TarefaPlanejamentoRequest;
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

    @PostMapping("/{id}/colunas")
    public ColunaPlanejamento criarColuna(@PathVariable Long id,
                                           @RequestBody ColunaPlanejamentoRequest request,
                                           @AuthenticationPrincipal Long usuarioId) {
        return service.criarColuna(id, request, usuarioId);
    }

    @GetMapping("/{id}/colunas/{colunaId}/tarefas")
    public List<TarefaPlanejamento> listarTarefas(@PathVariable Long id,
                                                  @PathVariable Long colunaId,
                                                  @AuthenticationPrincipal Long usuarioId) {
        return service.listarTarefas(id, colunaId, usuarioId);
    }

    @PostMapping("/{id}/colunas/{colunaId}/tarefas")
    public TarefaPlanejamento criarTarefa(@PathVariable Long id,
                                          @PathVariable Long colunaId,
                                          @RequestBody TarefaPlanejamentoRequest request,
                                          @AuthenticationPrincipal Long usuarioId) {
        return service.criarTarefa(id, colunaId, request, usuarioId);
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