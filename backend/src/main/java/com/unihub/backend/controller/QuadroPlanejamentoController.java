package com.unihub.backend.controller;

import com.unihub.backend.model.QuadroPlanejamento;
import com.unihub.backend.model.enums.QuadroStatus;
import com.unihub.backend.service.QuadroPlanejamentoService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<QuadroPlanejamento> listar(@AuthenticationPrincipal Long usuarioId,
                                           @RequestParam(value = "status", required = false) QuadroStatus status,
                                           @RequestParam(value = "titulo", required = false) String titulo) {
        return service.listar(usuarioId, status, titulo);
    }

    @GetMapping("/{id}")
    public QuadroPlanejamento buscarPorId(@PathVariable Long id, @AuthenticationPrincipal Long usuarioId) {
        return service.buscarPorId(id, usuarioId);
    }

    @PostMapping
    public QuadroPlanejamento criar(@RequestBody QuadroPlanejamento quadro,
                                    @AuthenticationPrincipal Long usuarioId) {
        return service.criar(quadro, usuarioId);
    }

    @PutMapping("/{id}")
    public QuadroPlanejamento atualizar(@PathVariable Long id,
                                        @RequestBody QuadroPlanejamento quadro,
                                        @AuthenticationPrincipal Long usuarioId) {
        return service.atualizar(id, quadro, usuarioId);
    }
}