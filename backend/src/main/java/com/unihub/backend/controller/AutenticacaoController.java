package com.unihub.backend.controller;

import com.unihub.backend.model.Usuario;
import com.unihub.backend.service.AutenticacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AutenticacaoController {

    @Autowired
    private AutenticacaoService servico;

    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        try {
            Usuario salvo = servico.registrar(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario usuario) {
        String token = servico.login(usuario.getNomeUsuario(), usuario.getSenha());
        if (token != null) {
            return ResponseEntity.ok(Map.of("token", token));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "Credenciais inválidas"));
    }
}