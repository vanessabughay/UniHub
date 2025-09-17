package com.unihub.backend.controller;

import com.unihub.backend.dto.ForgotPasswordRequest;
import com.unihub.backend.dto.LoginRequest;
import com.unihub.backend.dto.RegisterRequest;
import com.unihub.backend.dto.ResetPasswordRequest;
import com.unihub.backend.dto.LoginResponse;
import com.unihub.backend.model.Usuario;
import com.unihub.backend.service.AutenticacaoService;
import com.unihub.backend.service.PasswordResetService;

import jakarta.validation.Valid;

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
    private PasswordResetService passwordResetService;

    @Autowired
    private AutenticacaoService servico;

    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestBody RegisterRequest request) {
        try {
            Usuario usuario = new Usuario();
            usuario.setNomeUsuario(request.getName());
            usuario.setEmail(request.getEmail());
            usuario.setSenha(request.getPassword());
            servico.registrar(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        LoginResponse response = servico.login(request.getEmail(), request.getPassword());
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "Credenciais inválidas"));
    }

   @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        passwordResetService.requestReset(req);
    }

    
    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        passwordResetService.resetPassword(req);
    }

}