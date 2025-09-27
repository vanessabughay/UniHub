package com.unihub.backend.controller;

import com.unihub.backend.model.Ausencia;
import com.unihub.backend.service.AusenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/ausencias")
@CrossOrigin(origins = "*")
public class AusenciaController {

    @Autowired
    private AusenciaService service;

    @GetMapping
    public List<Ausencia> listarTodas(@AuthenticationPrincipal Long usuarioId) {
        return service.listarTodas(usuarioId);
    }

    @GetMapping("/{id}")
    public Ausencia buscarPorId(@PathVariable Long id, @AuthenticationPrincipal Long usuarioId) {
        return service.buscarPorId(id, usuarioId);
    }

    @PostMapping
    public Ausencia criar(@RequestBody Ausencia ausencia, @AuthenticationPrincipal Long usuarioId) {
        return service.salvar(ausencia, usuarioId);
    }

    @PutMapping("/{id}")
    public Ausencia atualizar(@PathVariable Long id, @RequestBody Ausencia novaAusencia, @AuthenticationPrincipal Long usuarioId) {
        Ausencia existente = service.buscarPorId(id, usuarioId);
        existente.setData(novaAusencia.getData());
        existente.setJustificativa(novaAusencia.getJustificativa());
        existente.setCategoria(novaAusencia.getCategoria());
        existente.setDisciplinaId(novaAusencia.getDisciplinaId());
        return service.salvar(existente, usuarioId);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id, @AuthenticationPrincipal Long usuarioId) {
        service.excluir(id, usuarioId);
    }
}