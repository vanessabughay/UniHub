package com.unihub.backend.controller;

import com.unihub.backend.model.Ausencia;
import com.unihub.backend.service.AusenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ausencias")
@CrossOrigin(origins = "*")
public class AusenciaController {

    @Autowired
    private AusenciaService service;

    @GetMapping
    public List<Ausencia> listarTodas() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public Ausencia buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public Ausencia criar(@RequestBody Ausencia ausencia) {
        return service.salvar(ausencia);
    }

    @PutMapping("/{id}")
    public Ausencia atualizar(@PathVariable Long id, @RequestBody Ausencia novaAusencia) {
        Ausencia existente = service.buscarPorId(id);
        existente.setData(novaAusencia.getData());
        existente.setJustificativa(novaAusencia.getJustificativa());
        existente.setCategoria(novaAusencia.getCategoria());
        existente.setDisciplinaId(novaAusencia.getDisciplinaId());
        return service.salvar(existente);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}