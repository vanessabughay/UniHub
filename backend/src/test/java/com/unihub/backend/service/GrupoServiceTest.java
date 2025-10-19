package com.unihub.backend.service;

import com.unihub.backend.model.Contato;
import com.unihub.backend.model.Grupo;
import com.unihub.backend.model.Usuario;
import com.unihub.backend.repository.ContatoRepository;
import com.unihub.backend.repository.GrupoRepository;
import com.unihub.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrupoServiceTest {

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private ContatoRepository contatoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private GrupoService grupoService;

    @Test
    void listarTodasDefineUsuarioComoAdministradorQuandoContatoPresente() {
        Long ownerId = 1L;

        Usuario usuario = new Usuario();
        usuario.setId(ownerId);
        usuario.setEmail("owner@example.com");

        Contato contatoUsuario = new Contato();
        contatoUsuario.setId(10L);
        contatoUsuario.setOwnerId(ownerId);
        contatoUsuario.setEmail("owner@example.com");
        contatoUsuario.setPendente(Boolean.FALSE);

        Contato outroContato = new Contato();
        outroContato.setId(20L);
        outroContato.setOwnerId(ownerId);
        outroContato.setEmail("friend@example.com");
        outroContato.setPendente(Boolean.FALSE);

        Grupo grupo = new Grupo();
        grupo.setOwnerId(ownerId);
        grupo.addMembro(contatoUsuario);
        grupo.addMembro(outroContato);

        when(grupoRepository.findByOwnerId(ownerId)).thenReturn(List.of(grupo));
        when(usuarioRepository.findById(ownerId)).thenReturn(Optional.of(usuario));
        when(contatoRepository.findByOwnerIdAndEmailIgnoreCase(ownerId, "owner@example.com"))
                .thenReturn(Optional.of(contatoUsuario));

        List<Grupo> resultado = grupoService.listarTodas(ownerId);

        assertEquals(contatoUsuario.getId(), resultado.get(0).getAdminContatoId());
    }

    @Test
    void listarTodasTrocaAdministradorQuandoUsuarioNaoEhMembroDoGrupo() {
        Long ownerId = 5L;

        Usuario usuario = new Usuario();
        usuario.setId(ownerId);
        usuario.setEmail("owner2@example.com");

        Contato contatoUsuario = new Contato();
        contatoUsuario.setId(50L);
        contatoUsuario.setOwnerId(ownerId);
        contatoUsuario.setEmail("owner2@example.com");
        contatoUsuario.setPendente(Boolean.FALSE);

        Contato membroA = new Contato();
        membroA.setId(2L);
        membroA.setOwnerId(ownerId);
        membroA.setEmail("membroa@example.com");
        membroA.setPendente(Boolean.FALSE);

        Contato membroB = new Contato();
        membroB.setId(3L);
        membroB.setOwnerId(ownerId);
        membroB.setEmail("membrob@example.com");
        membroB.setPendente(Boolean.FALSE);

        Grupo grupo = new Grupo();
        grupo.setOwnerId(ownerId);
        grupo.addMembro(membroA);
        grupo.addMembro(membroB);
        grupo.setAdminContatoId(contatoUsuario.getId());

        when(grupoRepository.findByOwnerId(ownerId)).thenReturn(List.of(grupo));
        when(usuarioRepository.findById(ownerId)).thenReturn(Optional.of(usuario));
        when(contatoRepository.findByOwnerIdAndEmailIgnoreCase(ownerId, "owner2@example.com"))
                .thenReturn(Optional.of(contatoUsuario));

        List<Grupo> resultado = grupoService.listarTodas(ownerId);

        assertEquals(membroA.getId(), resultado.get(0).getAdminContatoId());
    }
}