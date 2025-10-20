package com.unihub.backend.service;

import com.unihub.backend.model.Contato;
import com.unihub.backend.model.Grupo;
import com.unihub.backend.model.Usuario;
import com.unihub.backend.repository.ContatoRepository;
import com.unihub.backend.repository.GrupoRepository;
import com.unihub.backend.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GrupoServiceTest {

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private ContatoRepository contatoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private GrupoService grupoService;

    @BeforeEach
    void setUp() {
        lenient().when(usuarioRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
    }

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

        assertNull(resultado.get(0).getAdminContatoId());
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
        lenient().when(contatoRepository.findByOwnerIdAndIdIn(ownerId, List.of())).thenReturn(List.of());
        when(contatoRepository.findByOwnerIdAndEmail(ownerId, "owner3@example.com"))
                .thenReturn(Optional.of(contatoOwner));
        when(grupoRepository.save(any(Grupo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                grupoService.atualizarGrupo(grupoExistente.getId(), request, ownerId));

        assertEquals("Grupo removido por não possuir membros ativos.", exception.getMessage());
        verify(grupoRepository).delete(grupoExistente);
    }
@Test
    void excluirTransfereGrupoParaNovoOwnerQuandoHaMembrosAtivos() {
        Long ownerId = 10L;
        Long novoOwnerId = 20L;

        Usuario usuarioOwner = new Usuario();
        usuarioOwner.setId(ownerId);
        usuarioOwner.setEmail("dono@teste.com");

        Usuario usuarioNovoOwner = new Usuario();
        usuarioNovoOwner.setId(novoOwnerId);
        usuarioNovoOwner.setEmail("novo@teste.com");

        Contato contatoOwner = new Contato();
        contatoOwner.setId(101L);
        contatoOwner.setOwnerId(ownerId);
        contatoOwner.setEmail("dono@teste.com");
        contatoOwner.setPendente(Boolean.FALSE);
        contatoOwner.setIdContato(ownerId);

        Contato contatoNovoAdmin = new Contato();
        contatoNovoAdmin.setId(202L);
        contatoNovoAdmin.setOwnerId(ownerId);
        contatoNovoAdmin.setEmail("novo@teste.com");
        contatoNovoAdmin.setPendente(Boolean.FALSE);
        contatoNovoAdmin.setIdContato(novoOwnerId);

        Grupo grupo = new Grupo();
        grupo.setId(300L);
        grupo.setNome("Grupo Teste");
        grupo.setOwnerId(ownerId);
        grupo.addMembro(contatoOwner);
        grupo.addMembro(contatoNovoAdmin);
        grupo.setAdminContatoId(contatoOwner.getId());

        when(grupoRepository.findByIdAndOwnerId(grupo.getId(), ownerId)).thenReturn(Optional.of(grupo));
        when(usuarioRepository.findById(ownerId)).thenReturn(Optional.of(usuarioOwner));
        when(usuarioRepository.findById(novoOwnerId)).thenReturn(Optional.of(usuarioNovoOwner));
        when(grupoRepository.save(any(Grupo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        grupoService.excluir(grupo.getId(), ownerId);

        verify(grupoRepository, never()).delete(any(Grupo.class));
        verify(grupoRepository).save(grupo);
        assertEquals(novoOwnerId, grupo.getOwnerId());
        assertEquals(contatoNovoAdmin.getId(), grupo.getAdminContatoId());
        assertFalse(grupo.getMembros().contains(contatoOwner));
    }

    @Test
    void excluirTransfereGrupoQuandoNovoAdministradorTemApenasEmailAssociado() {
        Long ownerId = 12L;
        Long novoOwnerId = 22L;

        Usuario usuarioOwner = new Usuario();
        usuarioOwner.setId(ownerId);
        usuarioOwner.setEmail("owner-email@teste.com");

        Usuario usuarioNovoOwner = new Usuario();
        usuarioNovoOwner.setId(novoOwnerId);
        usuarioNovoOwner.setEmail("novo-email@teste.com");

        Contato contatoOwner = new Contato();
        contatoOwner.setId(121L);
        contatoOwner.setOwnerId(ownerId);
        contatoOwner.setEmail("owner-email@teste.com");
        contatoOwner.setPendente(Boolean.FALSE);
        contatoOwner.setIdContato(ownerId);

        Contato contatoNovoAdmin = new Contato();
        contatoNovoAdmin.setId(222L);
        contatoNovoAdmin.setOwnerId(ownerId);
        contatoNovoAdmin.setEmail("novo-email@teste.com");
        contatoNovoAdmin.setPendente(Boolean.FALSE);

        Grupo grupo = new Grupo();
        grupo.setId(333L);
        grupo.setNome("Grupo Email");
        grupo.setOwnerId(ownerId);
        grupo.addMembro(contatoOwner);
        grupo.addMembro(contatoNovoAdmin);
        grupo.setAdminContatoId(contatoOwner.getId());

        when(grupoRepository.findByIdAndOwnerId(grupo.getId(), ownerId)).thenReturn(Optional.of(grupo));
        when(usuarioRepository.findById(ownerId)).thenReturn(Optional.of(usuarioOwner));
        when(usuarioRepository.findByEmailIgnoreCase("novo-email@teste.com")).thenReturn(Optional.of(usuarioNovoOwner));
        when(grupoRepository.save(any(Grupo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        grupoService.excluir(grupo.getId(), ownerId);

        verify(grupoRepository, never()).delete(any(Grupo.class));
        verify(grupoRepository).save(grupo);
        assertEquals(novoOwnerId, grupo.getOwnerId());
        assertEquals(contatoNovoAdmin.getId(), grupo.getAdminContatoId());
        assertEquals(novoOwnerId, contatoNovoAdmin.getIdContato());
        assertFalse(grupo.getMembros().contains(contatoOwner));
    }

    @Test
    void excluirRemoveGrupoQuandoNaoHaOutroMembroAtivo() {
        Long ownerId = 11L;

        Usuario usuarioOwner = new Usuario();
        usuarioOwner.setId(ownerId);
        usuarioOwner.setEmail("dono2@teste.com");

        Contato contatoOwner = new Contato();
        contatoOwner.setId(111L);
        contatoOwner.setOwnerId(ownerId);
        contatoOwner.setEmail("dono2@teste.com");
        contatoOwner.setPendente(Boolean.FALSE);
        contatoOwner.setIdContato(ownerId);

        Grupo grupo = new Grupo();
        grupo.setId(400L);
        grupo.setNome("Grupo Sem Membros");
        grupo.setOwnerId(ownerId);
        grupo.addMembro(contatoOwner);

        when(grupoRepository.findByIdAndOwnerId(grupo.getId(), ownerId)).thenReturn(Optional.of(grupo));
        when(usuarioRepository.findById(ownerId)).thenReturn(Optional.of(usuarioOwner));

        grupoService.excluir(grupo.getId(), ownerId);

        verify(grupoRepository).delete(grupo);
        verify(grupoRepository, never()).save(any(Grupo.class));
    }
}