package com.unihub.backend.controller;

import com.unihub.backend.model.Contato;
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
        return service.listarTodas(); // deve filtrar por owner no service
    }

    @GetMapping("/{id:\\d+}")
    public Contato buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public Contato criar(@RequestBody Contato contato) {
        contato.setId(null);             
        return service.salvar(contato);
    }

    @PutMapping("/{id:\\d+}")
    public Contato atualizar(@PathVariable Long id, @RequestBody Contato novoContato) {
        Contato existente = service.buscarPorId(id); 
        existente.setNome(novoContato.getNome());
        existente.setPendente(novoContato.getPendente());
        return service.salvar(existente);
    }

    @DeleteMapping("/{id:\\d+}")
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    @PostMapping("/pendentes/{id:\\d+}/aceitar")
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void aceitarConvite(@PathVariable Long id) {
        service.aceitarConvite(id);
    }

    @PostMapping("/pendentes/{id:\\d+}/rejeitar")
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void rejeitarConvite(@PathVariable Long id) {
        service.rejeitarConvite(id);
    }

    @GetMapping("/pesquisa")
    public List<Contato> buscarPorNome(@RequestParam String nome) {
        return service.buscarPorNome(nome);
    }

    @GetMapping("/pendentes")
    public List<Contato> buscarPendentesPorEmail(@RequestParam String email) {
        return service.buscarPendentesPorEmail(email);
    }
}
