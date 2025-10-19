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

import jakarta.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

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


    @Test
    void atualizarGrupoTransferePropriedadeParaNovoAdministrador() {
        Long ownerId = 1L;
        Long novoAdministradorUsuarioId = 2L;

        Usuario usuarioOwner = new Usuario();
        usuarioOwner.setId(ownerId);
        usuarioOwner.setEmail("owner@example.com");

        Usuario usuarioNovoAdministrador = new Usuario();
        usuarioNovoAdministrador.setId(novoAdministradorUsuarioId);
        usuarioNovoAdministrador.setEmail("novo@example.com");

        Contato contatoOwner = new Contato();
        contatoOwner.setId(11L);
        contatoOwner.setOwnerId(ownerId);
        contatoOwner.setEmail("owner@example.com");
        contatoOwner.setPendente(Boolean.FALSE);

        Contato contatoNovoAdmin = new Contato();
        contatoNovoAdmin.setId(12L);
        contatoNovoAdmin.setOwnerId(ownerId);
        contatoNovoAdmin.setEmail("novo@example.com");
        contatoNovoAdmin.setIdContato(novoAdministradorUsuarioId);
        contatoNovoAdmin.setPendente(Boolean.FALSE);

        Grupo grupoExistente = new Grupo();
        grupoExistente.setId(100L);
        grupoExistente.setNome("Grupo Teste");
        grupoExistente.setOwnerId(ownerId);
        grupoExistente.addMembro(contatoOwner);
        grupoExistente.addMembro(contatoNovoAdmin);
        grupoExistente.setAdminContatoId(contatoOwner.getId());

        Grupo request = new Grupo();
        request.setMembros(List.of(contatoNovoAdmin));

        when(grupoRepository.findByIdAndOwnerId(grupoExistente.getId(), ownerId))
                .thenReturn(Optional.of(grupoExistente));
        when(usuarioRepository.findById(ownerId)).thenReturn(Optional.of(usuarioOwner));
        when(usuarioRepository.findById(novoAdministradorUsuarioId)).thenReturn(Optional.of(usuarioNovoAdministrador));
        when(contatoRepository.findByOwnerIdAndIdIn(ownerId, List.of(contatoNovoAdmin.getId())))
                .thenReturn(List.of(contatoNovoAdmin));
        when(contatoRepository.findByOwnerIdAndEmail(ownerId, "owner@example.com"))
                .thenReturn(Optional.of(contatoOwner));
        when(contatoRepository.findByOwnerIdAndEmailIgnoreCase(ownerId, "owner@example.com"))
                .thenReturn(Optional.of(contatoOwner));
        when(grupoRepository.save(any(Grupo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Grupo atualizado = grupoService.atualizarGrupo(grupoExistente.getId(), request, ownerId);

        assertEquals(contatoNovoAdmin.getId(), atualizado.getAdminContatoId());
        assertEquals(novoAdministradorUsuarioId, atualizado.getOwnerId());
        verify(grupoRepository, never()).delete(any(Grupo.class));
    }

    @Test
    void atualizarGrupoExcluiQuandoNaoHaMembros() {
        Long ownerId = 3L;

        Usuario usuario = new Usuario();
        usuario.setId(ownerId);
        usuario.setEmail("owner3@example.com");

        Contato contatoOwner = new Contato();
        contatoOwner.setId(30L);
        contatoOwner.setOwnerId(ownerId);
        contatoOwner.setEmail("owner3@example.com");
        contatoOwner.setPendente(Boolean.FALSE);

        Grupo grupoExistente = new Grupo();
        grupoExistente.setId(200L);
        grupoExistente.setNome("Grupo Vazio");
        grupoExistente.setOwnerId(ownerId);
        grupoExistente.addMembro(contatoOwner);

        Grupo request = new Grupo();
        request.setMembros(List.of());

        when(grupoRepository.findByIdAndOwnerId(grupoExistente.getId(), ownerId))
                .thenReturn(Optional.of(grupoExistente));
        when(usuarioRepository.findById(ownerId)).thenReturn(Optional.of(usuario));
        when(contatoRepository.findByOwnerIdAndIdIn(ownerId, List.of())).thenReturn(List.of());
        when(contatoRepository.findByOwnerIdAndEmail(ownerId, "owner3@example.com"))
                .thenReturn(Optional.of(contatoOwner));
        when(grupoRepository.save(any(Grupo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                grupoService.atualizarGrupo(grupoExistente.getId(), request, ownerId));

        assertEquals("Grupo removido por não possuir membros ativos.", exception.getMessage());
        verify(grupoRepository).delete(grupoExistente);
    }
}