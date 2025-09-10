package com.unihub.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import com.unihub.backend.model.Usuario;
import com.unihub.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.unihub.backend.dto.LoginResponse;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class AutenticacaoService {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Map<String, Long> tokens = new ConcurrentHashMap<>();

    public Usuario registrar(Usuario usuario) {
        Optional<Usuario> existente = repository.findByEmail(usuario.getEmail());
        if (existente.isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }
         usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return repository.save(usuario);
    }

    public LoginResponse login(String email, String senha) {
        Optional<Usuario> usuarioOpt = repository.findByEmail(email);
        if (usuarioOpt.isPresent() && passwordEncoder.matches(senha, usuarioOpt.get().getSenha())) {
            String token = UUID.randomUUID().toString();
            tokens.put(token, usuarioOpt.get().getId());
            return new LoginResponse(token, usuarioOpt.get().getNomeUsuario());
        }
        return null;
    }
    
    public Long getUsuarioIdPorToken(String token) {
        return tokens.get(token);
    }
}