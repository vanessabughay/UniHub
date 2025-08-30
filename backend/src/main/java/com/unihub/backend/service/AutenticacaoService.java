package com.unihub.backend.service;

import com.unihub.backend.model.Usuario;
import com.unihub.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AutenticacaoService {

    @Autowired
    private UsuarioRepository repository;

    public Usuario registrar(Usuario usuario) {
        Optional<Usuario> existente = repository.findByEmail(usuario.getEmail());
        if (existente.isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }
        return repository.save(usuario);
    }

    public String login(String email, String senha) {
        Optional<Usuario> usuarioOpt = repository.findByEmail(email);
        if (usuarioOpt.isPresent() && usuarioOpt.get().getSenha().equals(senha)) {
            return UUID.randomUUID().toString();
        }
        return null;
    }
}