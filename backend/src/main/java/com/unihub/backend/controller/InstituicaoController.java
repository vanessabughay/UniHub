package com.unihub.backend.controller;

import com.unihub.backend.model.Instituicao;
import com.unihub.backend.service.InstituicaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/instituicoes")
@CrossOrigin(origins = "*")
public class InstituicaoController {

    @Autowired
    private InstituicaoService service;

    @GetMapping
    public List<Instituicao> listar(@RequestParam(required = false) String nome) {
        if (nome != null && !nome.isEmpty()) {
            return service.buscarPorNome(nome);
        }
        return service.listarTodas();
    }

    @PostMapping
    public Instituicao criar(@RequestBody Instituicao instituicao) {
        return service.salvar(instituicao);
    }

    @PutMapping("/{id}")
    public Instituicao atualizar(@PathVariable Long id, @RequestBody Instituicao instituicao) {
        return service.atualizar(id, instituicao);
    }
}