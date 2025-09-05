package com.unihub.backend.controller;

import com.unihub.backend.model.Grupo;
import com.unihub.backend.service.GrupoService;

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
    @RequestMapping("/grupo")
    @CrossOrigin(origins = "*")

    public class GrupoController {
        @Autowired
        private GrupoService service;

        @GetMapping
        public List<Grupo> listarTodas() {
            return service.listarTodas();
        }

        @GetMapping("/{id}")
        public Grupo buscarPorId(@PathVariable Long id) {
            return service.buscarPorId(id);
        }

        @PostMapping
        public Grupo criar(@RequestBody Grupo grupo) {
            return service.salvar(grupo);
        }

        @PutMapping("/{id}")
        public Grupo atualizar(@PathVariable Long id, @RequestBody Grupo novoGrupo) {
            Grupo existente = service.buscarPorId(id);


            existente.setNome(novoGrupo.getNome());
            existente.setId(novoGrupo.getId());
            existente.setContatoLista(novoGrupo.getContatoLista());

            return service.salvar(existente);
        }

        @DeleteMapping("/{id}")
        public void excluir(@PathVariable Long id) {
            service.excluir(id);
        }

        @GetMapping("/pesquisa")
        public List<Grupo> buscarPorNome(@RequestParam String nome) {
            return service.buscarPorNome(nome);
        }
}
