package com.unihub.backend.service;

import com.unihub.backend.model.Categoria;
import com.unihub.backend.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.unihub.backend.model.Usuario;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository repository;

   public List<Categoria> listarTodas(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    public Categoria salvar(Categoria categoria, Long usuarioId) {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        categoria.setUsuario(usuario);
        return repository.save(categoria);
    }

   public Categoria buscarOuCriar(String nome, Long usuarioId) {
        return repository.findByNomeAndUsuarioId(nome, usuarioId).orElseGet(() -> {
            Categoria nova = new Categoria();
            nova.setNome(nome);
            Usuario usuario = new Usuario();
            usuario.setId(usuarioId);
            nova.setUsuario(usuario);
            return repository.save(nova);
        });
    }
}