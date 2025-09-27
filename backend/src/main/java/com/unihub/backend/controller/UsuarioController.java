package com.unihub.backend.controller;

import com.unihub.backend.dto.UpdateUsuarioRequest;
import com.unihub.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PutMapping("/me")
    public ResponseEntity<?> atualizarUsuario(@RequestBody UpdateUsuarioRequest request,
                                              @AuthenticationPrincipal Long usuarioId) {
        return repository.findById(usuarioId)
                .map(usuario -> {
                    if (request.getName() != null && !request.getName().isBlank()) {
                        usuario.setNomeUsuario(request.getName());
                    }
                    if (request.getEmail() != null && !request.getEmail().isBlank()) {
                        usuario.setEmail(request.getEmail());
                    }
                    if (request.getPassword() != null && !request.getPassword().isBlank()) {
                        usuario.setSenha(passwordEncoder.encode(request.getPassword()));
                    }
                    repository.save(usuario);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}