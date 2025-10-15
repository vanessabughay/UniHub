package com.unihub.backend.service;

import com.unihub.backend.model.Contato;
import com.unihub.backend.repository.ContatoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContatoService {
    @Autowired
    private ContatoRepository repository;

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        return (auth.getPrincipal() instanceof Long)
                ? (Long) auth.getPrincipal()
                : Long.valueOf(auth.getName());
    }

    public List<Contato> listarTodas() {
        return repository.findByOwnerId(currentUserId());
    }

    public Contato salvar(Contato contato) {
        contato.setOwnerId(currentUserId());
        return repository.save(contato);
    }

    public Contato buscarPorId(Long id) {
        return repository.findByIdAndOwnerId(id, currentUserId())
                .orElseThrow(() -> new RuntimeException("Contato não encontrado"));
    }

    public void excluir(Long id) {
        if (!repository.existsByIdAndOwnerId(id, currentUserId()))
            throw new RuntimeException("Sem acesso");
        repository.deleteById(id);
    }

    public List<Contato> buscarPorNome(String nome) {
        Long ownerId = currentUserId();
        if (ownerId == null) {
            return List.of();
        }

        List<Contato> contatos = repository.findByOwnerId(ownerId);

        if (nome == null || nome.isBlank()) {
            return contatos;
        }

        String termoNormalizado = nome.toLowerCase();
        return contatos.stream()
                .filter(
                        contato ->
                                contato.getNome() != null
                                        && contato.getNome().toLowerCase().contains(termoNormalizado))
                .collect(Collectors.toList());
    }

}
