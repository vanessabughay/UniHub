package com.unihub.backend.controller;

import com.unihub.backend.dto.AvaliacaoRequest;        // <- DTO
import com.unihub.backend.model.Avaliacao;
import com.unihub.backend.service.AvaliacaoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.*;
import org.springframework.security.core.Authentication;   // <- pegar usuarioId do TokenFilter
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/avaliacoes")
@CrossOrigin(origins = "*")
public class AvaliacaoController {

    private static final Logger logger = LoggerFactory.getLogger(AvaliacaoController.class);
    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @GetMapping
    public ResponseEntity<List<Avaliacao>> listarTodos() {
        return ResponseEntity.ok(avaliacaoService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Avaliacao> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(avaliacaoService.buscarPorId(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> criar(@RequestBody AvaliacaoRequest req, Authentication auth) {
        // se você não usa usuarioId aqui, pode remover o Authentication
        Long usuarioId = auth != null ? (Long) auth.getPrincipal() : null;
        Long id = avaliacaoService.criar(req, usuarioId);
        logger.info("Avaliação criada id={}", id);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @PutMapping(path="//{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> atualizar(@PathVariable Long id,
                                       @RequestBody AvaliacaoRequest req,
                                       Authentication auth) {
        Long usuarioId = auth != null ? (Long) auth.getPrincipal() : null;
        boolean ok = avaliacaoService.atualizar(id, req, usuarioId);
        return ok ? ResponseEntity.ok(Map.of("id", id))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        avaliacaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
