package com.unihub.backend.controller;

import com.unihub.backend.model.Grupo;
import com.unihub.backend.service.GrupoService;
import jakarta.persistence.EntityNotFoundException; // Para tratar exceções do service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Para códigos de status HTTP
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Importação curinga para anotações do Spring Web

import java.util.List;

@RestController
@RequestMapping("/api/grupos") // Boa prática usar um prefixo como /api
@CrossOrigin(origins = "*") // Para desenvolvimento; restrinja em produção
public class GrupoController {

    @Autowired
    private GrupoService grupoService; // Renomeado para clareza

    @GetMapping
    public ResponseEntity<List<Grupo>> listarTodos() {
        List<Grupo> grupos = grupoService.listarTodas();
        return ResponseEntity.ok(grupos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Grupo> buscarPorId(@PathVariable Long id) {
        try {
            Grupo grupo = grupoService.buscarPorId(id);
            return ResponseEntity.ok(grupo);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Grupo> criar(@RequestBody Grupo grupo) {
        // Validações básicas podem ser adicionadas aqui ou no service
        if (grupo.getNome() == null || grupo.getNome().trim().isEmpty()) {
            // Você pode lançar uma exceção customizada ou retornar BadRequest
            return ResponseEntity.badRequest().build(); // Exemplo simples
        }
        try {
            Grupo novoGrupo = grupoService.criarGrupo(grupo);
            // Retorna 201 Created com o objeto criado e o Location header (idealmente)
            return ResponseEntity.status(HttpStatus.CREATED).body(novoGrupo);
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            // Se criarGrupo lançar exceções por IDs de membros inválidos, etc.
            return ResponseEntity.badRequest().body(null); // Simplificado, idealmente retornaria uma mensagem de erro
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Grupo> atualizar(@PathVariable Long id, @RequestBody Grupo grupoDetalhesRequest) {
        // Validações básicas
        if (grupoDetalhesRequest.getNome() != null && grupoDetalhesRequest.getNome().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null); // Nome não pode ser vazio se fornecido
        }
        try {
            Grupo grupoAtualizado = grupoService.atualizarGrupo(id, grupoDetalhesRequest);
            return ResponseEntity.ok(grupoAtualizado);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            // Ex: se um ID de contato fornecido para atualização não for válido
            return ResponseEntity.badRequest().body(null); // Simplificado
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        try {
            grupoService.excluir(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/pesquisa")
    public ResponseEntity<List<Grupo>> buscarPorNome(@RequestParam String nome) {
        List<Grupo> grupos = grupoService.buscarPorNome(nome);
        if (grupos.isEmpty()) {
            return ResponseEntity.noContent().build(); // Ou ok com lista vazia, dependendo da preferência
        }
        return ResponseEntity.ok(grupos);
    }
}
