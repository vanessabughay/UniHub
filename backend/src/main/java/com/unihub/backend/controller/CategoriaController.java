package com.unihub.backend.controller;

import com.unihub.backend.model.Categoria;
import com.unihub.backend.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/categorias")
@CrossOrigin(origins = "*")
public class CategoriaController {

    @Autowired
    private CategoriaService service;

    @GetMapping
    public List<Categoria> listarTodas(@AuthenticationPrincipal Long usuarioId) {
        return service.listarTodas(usuarioId);
    }

    @PostMapping
    public Categoria criar(@RequestBody Categoria categoria, @AuthenticationPrincipal Long usuarioId) {
        return service.salvar(categoria, usuarioId);
    }
}