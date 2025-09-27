package com.unihub.backend.controller;

import com.unihub.backend.model.Instituicao;
import com.unihub.backend.service.InstituicaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/instituicoes")
@CrossOrigin(origins = "*")
public class InstituicaoController {

    @Autowired
    private InstituicaoService service;

    @GetMapping
    public List<Instituicao> listar(@RequestParam(required = false) String nome, @AuthenticationPrincipal Long usuarioId) {
        return service.buscarPorNome(nome, usuarioId);
    }

    @PostMapping
    public Instituicao criar(@RequestBody Instituicao instituicao, @AuthenticationPrincipal Long usuarioId) {
        return service.salvar(instituicao, usuarioId);
    }

    @PutMapping("/{id}")
    public Instituicao atualizar(@PathVariable Long id, @RequestBody Instituicao instituicao, @AuthenticationPrincipal Long usuarioId) {
        return service.atualizar(id, instituicao, usuarioId);
    }
}