package com.unihub.backend.controller;

import com.unihub.backend.dto.anotacoes.AnotacoesRequest;
import com.unihub.backend.dto.anotacoes.AnotacoesResponse;
import com.unihub.backend.service.AnotacoesService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/disciplinas/{disciplinaId}/anotacoes")
public class AnotacoesController {

    private final AnotacoesService service;

    public AnotacoesController(AnotacoesService service) {
        this.service = service;
    }

    @GetMapping
    public Page<AnotacoesResponse> listar(@PathVariable Long disciplinaId, Pageable pageable) {
        return service.listar(disciplinaId, pageable);
    }

    @GetMapping("/{id}")
    public AnotacoesResponse obter(@PathVariable Long disciplinaId, @PathVariable Long id) {
        return service.obter(disciplinaId, id);
    }

    @PostMapping
    public ResponseEntity<AnotacoesResponse> criar(
            @PathVariable Long disciplinaId,
            @Valid @RequestBody AnotacoesRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(disciplinaId, req));
    }

    @PutMapping("/{id}")
    public AnotacoesResponse atualizar(
            @PathVariable Long disciplinaId,
            @PathVariable Long id,
            @Valid @RequestBody AnotacoesRequest req
    ) {
        return service.atualizar(disciplinaId, id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long disciplinaId, @PathVariable Long id) {
        service.remover(disciplinaId, id);
    }
}