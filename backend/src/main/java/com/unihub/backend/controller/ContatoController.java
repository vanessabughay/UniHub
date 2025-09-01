package com.unihub.backend.controller;

import com.unihub.backend.model.Contato;
import com.unihub.backend.model.HorarioAula;
import com.unihub.backend.service.ContatoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/contato")
@CrossOrigin(origins = "*")

public class ContatoController {
    @Autowired
    private ContatoService service;

    @GetMapping
    public List<Contato> listarTodas() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public Contato buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public Contato criar(@RequestBody Contato contato) {
        return service.salvar(contato);
    }

    @PutMapping("/{id}")
    public Contato atualizar(@PathVariable Long id, @RequestBody Contato novoContato) {
        Contato existente = service.buscarPorId(id);


        existente.setNome(novoContato.getNome());
        existente.setId(novoContato.getId());
        existente.setEmail(novoContato.getEmail());

        return service.salvar(existente);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    @GetMapping("/pesquisa")
    public List<Contato> buscarPorNome(@RequestParam String nome) {
        return service.buscarPorNome(nome);
    }
}
