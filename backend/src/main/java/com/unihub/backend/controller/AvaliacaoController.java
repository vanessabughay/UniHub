package com.unihub.backend.controller;

import com.unihub.backend.model.Avaliacao;
import com.unihub.backend.service.AvaliacaoService;
import jakarta.persistence.EntityNotFoundException;

// Imports CORRIGIDOS/ADICIONADOS para Logging e Jackson
import org.slf4j.Logger;                        // <<< USE ESTE
import org.slf4j.LoggerFactory;                 // <<< USE ESTE
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Para ObjectMapper logar datas corretamente

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
// Removido: import java.util.logging.Logger; // Não use este, use org.slf4j.Logger

@RestController
@RequestMapping("/api/avaliacoes")
@CrossOrigin(origins = "*")
public class AvaliacaoController {

    // Logger estático e final para a classe (correto com os imports certos)
    private static final Logger logger = LoggerFactory.getLogger(AvaliacaoController.class);

    private final ObjectMapper jacksonObjectMapper; // ObjectMapper como campo final

    @Autowired
    private AvaliacaoService avaliacaoService;

    // Construtor para inicializar o ObjectMapper com o JavaTimeModule
    public AvaliacaoController() {
        this.jacksonObjectMapper = new ObjectMapper();
        this.jacksonObjectMapper.registerModule(new JavaTimeModule());
        // Opcional: para datas como "2024-09-22" em vez de timestamps no log JSON
        // this.jacksonObjectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @GetMapping
    public ResponseEntity<List<Avaliacao>> listarTodos() {
        logger.debug("Requisição para listar todas as avaliações");
        List<Avaliacao> avaliacoes = avaliacaoService.listarTodas();
        return ResponseEntity.ok(avaliacoes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Avaliacao> buscarPorId(@PathVariable Long id) {
        logger.debug("Requisição para buscar avaliação por ID: {}", id);
        try {
            Avaliacao avaliacao = avaliacaoService.buscarPorId(id);
            return ResponseEntity.ok(avaliacao);
        } catch (EntityNotFoundException e) {
            logger.warn("Avaliação não encontrada para o ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Avaliacao avaliacao) { // Alterado para ResponseEntity<?>
        logger.info("Recebida requisição para criar Avaliacao.");
        try {
            // Log do objeto Java como o Jackson o desserializou
            logger.debug("Avaliacao recebida no controller (objeto Java toString()): {}", avaliacao); // Passa o objeto

            // Log do objeto como uma string JSON
            logger.debug("Avaliacao recebida no controller (representação JSON via ObjectMapper): {}",
                    jacksonObjectMapper.writeValueAsString(avaliacao)); // Passa a string JSON
        } catch (Exception e) {
            logger.error("Erro ao tentar logar a avaliacao recebida para criação", e);
        }

        // Validações explícitas (MELHORADAS)
        if (avaliacao.getDescricao() == null || avaliacao.getDescricao().trim().isEmpty()) {
            logger.warn("Falha na validação: Descrição é obrigatória. Descrição recebida: '{}'", avaliacao.getDescricao());
            return ResponseEntity.badRequest().body("Descrição é obrigatória.");
        }
        if (avaliacao.getModalidade() == null) {
            logger.warn("Falha na validação: Modalidade é obrigatória. Modalidade recebida: null. Payload completo: {}", avaliacao);
            return ResponseEntity.badRequest().body("Modalidade é obrigatória.");
        }
        if (avaliacao.getPrioridade() == null) {
            logger.warn("Falha na validação: Prioridade é obrigatória. Prioridade recebida: null. Payload completo: {}", avaliacao);
            return ResponseEntity.badRequest().body("Prioridade é obrigatória.");
        }
        if (avaliacao.getEstado() == null) { // Assumindo que estado também é NOT NULL no BD
            logger.warn("Falha na validação: Estado é obrigatório. Estado recebido: null. Payload completo: {}", avaliacao);
            return ResponseEntity.badRequest().body("Estado é obrigatório.");
        }
        // Se data_entrega é NOT NULL no banco (e você não mudou para nullable=true no backend)
        // Adicione uma validação aqui se ela for obrigatória:
        // if (avaliacao.getDataEntrega() == null) {
        //     logger.warn("Falha na validação: Data de entrega é obrigatória. DataEntrega recebida: null. Payload: {}", avaliacao);
        //     return ResponseEntity.badRequest().body("Data de entrega é obrigatória.");
        // }


        try {
            Avaliacao novoAvaliacao = avaliacaoService.criarAvaliacao(avaliacao);
            logger.info("Avaliação criada com sucesso: ID {}", novoAvaliacao.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(novoAvaliacao);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de argumento ao criar avaliação: {}. Payload: {}", e.getMessage(), avaliacao);
            return ResponseEntity.badRequest().body("Dados inválidos: " + e.getMessage());
        } catch (EntityNotFoundException e) {
            logger.warn("Entidade não encontrada durante criação da avaliação: {}. Payload: {}", e.getMessage(), avaliacao);
            return ResponseEntity.badRequest().body("Entidade relacionada não encontrada: " + e.getMessage());
        }
        // DataIntegrityViolationException será tratada pelo Spring e resultará em um 500, o que é ok.
        // As validações acima tentam pegar os problemas antes.
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Avaliacao avaliacaoDetalhesRequest) {
        logger.info("Recebida requisição para atualizar Avaliacao ID: {}", id);
        try {
            logger.debug("Payload da Avaliacao para atualização (objeto Java toString()): {}", avaliacaoDetalhesRequest);
            logger.debug("Payload da Avaliacao para atualização (representação JSON via ObjectMapper): {}",
                    jacksonObjectMapper.writeValueAsString(avaliacaoDetalhesRequest));
        } catch (Exception e) {
            logger.error("Erro ao tentar logar a avaliacao recebida para atualização", e);
        }

        // Adicione validações similares ao 'criar' se campos obrigatórios não podem se tornar nulos na atualização
        if (avaliacaoDetalhesRequest.getDescricao() != null && avaliacaoDetalhesRequest.getDescricao().trim().isEmpty()) {
            logger.warn("Falha na validação (atualizar): Descrição não pode ser vazia se fornecida. ID: {}", id);
            return ResponseEntity.badRequest().body("Descrição não pode ser vazia se fornecida.");
        }
        // Exemplo:
        // if (avaliacaoDetalhesRequest.getModalidade() == null) {
        //     logger.warn("Falha na validação (atualizar): Modalidade não pode ser nula. ID: {}, Payload: {}", id, avaliacaoDetalhesRequest);
        //     return ResponseEntity.badRequest().body("Modalidade é obrigatória.");
        // }


        try {
            Avaliacao avaliacaoAtualizado = avaliacaoService.atualizarAvaliacao(id, avaliacaoDetalhesRequest);
            logger.info("Avaliação ID {} atualizada com sucesso.", id);
            return ResponseEntity.ok(avaliacaoAtualizado);
        } catch (EntityNotFoundException e) {
            logger.warn("Avaliação não encontrada para atualização. ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de argumento ao atualizar avaliação ID {}: {}. Payload: {}", id, e.getMessage(), avaliacaoDetalhesRequest);
            return ResponseEntity.badRequest().body("Dados inválidos: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        logger.info("Recebida requisição para excluir Avaliacao ID: {}", id);
        try {
            avaliacaoService.excluir(id);
            logger.info("Avaliação ID {} excluída com sucesso.", id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            logger.warn("Avaliação não encontrada para exclusão. ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/pesquisa")
    public ResponseEntity<List<Avaliacao>> buscarPorNome(@RequestParam String descricao) {
        logger.debug("Requisição para pesquisar avaliações por descrição: {}", descricao);
        List<Avaliacao> avaliacoes = avaliacaoService.buscarPorNome(descricao);
        if (avaliacoes.isEmpty()) {
            logger.debug("Nenhuma avaliação encontrada para a descrição: {}", descricao);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(avaliacoes);
    }
}

