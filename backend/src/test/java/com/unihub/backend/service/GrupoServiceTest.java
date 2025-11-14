package com.unihub.backend.service;

import com.unihub.backend.model.Contato;
import com.unihub.backend.dto.notificacoes.NotificacaoLogRequest;
import com.unihub.backend.model.Grupo;
import com.unihub.backend.model.NotificacaoConfiguracao;
import com.unihub.backend.model.Usuario;
import com.unihub.backend.repository.ContatoRepository;
import com.unihub.backend.repository.GrupoRepository;
import com.unihub.backend.repository.NotificacaoConfiguracaoRepository;
import com.unihub.backend.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GrupoServiceTest {

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private ContatoRepository contatoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private NotificacaoConfiguracaoRepository notificacaoConfiguracaoRepository;

    @InjectMocks
    private GrupoService grupoService;

    @BeforeEach
    void setUp() {
        lenient().when(usuarioRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        lenient().when(notificacaoConfiguracaoRepository.findByUsuarioId(anyLong())).thenReturn(Optional.empty());
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
    void criarGrupoRegistraNotificacaoParaNovosMembros() {
        Long ownerId = 1L;
        Long membroUsuarioId = 2L;

        Usuario owner = new Usuario();
        owner.setId(ownerId);
        owner.setEmail("owner@example.com");
        owner.setNomeUsuario("Owner");

        Usuario usuarioMembro = new Usuario();
        usuarioMembro.setId(membroUsuarioId);
        usuarioMembro.setEmail("member@example.com");

        Contato contatoOwner = new Contato();
        contatoOwner.setId(10L);
        contatoOwner.setOwnerId(ownerId);
        contatoOwner.setEmail(owner.getEmail());
        contatoOwner.setIdContato(ownerId);
        contatoOwner.setPendente(Boolean.FALSE);

        Contato contatoMembro = new Contato();
        contatoMembro.setId(20L);
        contatoMembro.setOwnerId(ownerId);
        contatoMembro.setEmail("member@example.com");
        contatoMembro.setIdContato(membroUsuarioId);
        contatoMembro.setPendente(Boolean.FALSE);

        Grupo grupoRequest = new Grupo();
        grupoRequest.setNome("Novo Grupo");
        grupoRequest.setMembros(List.of(contatoMembro));

        when(usuarioRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(usuarioRepository.findById(membroUsuarioId)).thenReturn(Optional.of(usuarioMembro));
        when(contatoRepository.findByOwnerIdAndEmail(ownerId, owner.getEmail())).thenReturn(Optional.of(contatoOwner));
        when(contatoRepository.findByOwnerIdAndIdIn(eq(ownerId), anyList())).thenReturn(List.of(contatoMembro));
        when(contatoRepository.findByOwnerIdAndIdContato(ownerId, membroUsuarioId)).thenReturn(Optional.of(contatoMembro));
        when(contatoRepository.findByOwnerIdAndEmailIgnoreCase(ownerId, "member@example.com"))
                .thenReturn(Optional.of(contatoMembro));
        when(grupoRepository.save(any(Grupo.class))).thenAnswer(invocation -> {
            Grupo value = invocation.getArgument(0);
            if (value.getId() == null) {
                value.setId(500L);
            }
            return value;
        });
        when(notificacaoConfiguracaoRepository.findByUsuarioId(membroUsuarioId)).thenReturn(Optional.of(configuracao(true)));
        when(notificacaoService.registrarNotificacao(anyLong(), any(NotificacaoLogRequest.class))).thenReturn(null);

        Grupo resultado = grupoService.criarGrupo(grupoRequest, ownerId);

        assertEquals(500L, resultado.getId());
        ArgumentCaptor<NotificacaoLogRequest> captor = ArgumentCaptor.forClass(NotificacaoLogRequest.class);
        verify(notificacaoService).registrarNotificacao(eq(membroUsuarioId), captor.capture());
        NotificacaoLogRequest request = captor.getValue();
        assertEquals("GRUPO_MEMBRO_ADICIONADO", request.getTipo());
        assertEquals("GRUPO_MEMBRO_ADICIONADO", request.getCategoria());
        assertEquals(500L, request.getReferenciaId());
        assertTrue(request.getMensagem().contains("Novo Grupo"));
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
    void atualizarGrupoNaoRegistraNotificacaoQuandoConfiguracaoDesativada() {
        Long ownerId = 4L;
        Long grupoId = 900L;
        Long novoUsuarioId = 40L;

        Usuario owner = new Usuario();
        owner.setId(ownerId);
        owner.setEmail("owner-notify@example.com");

        Usuario usuarioNovo = new Usuario();
        usuarioNovo.setId(novoUsuarioId);
        usuarioNovo.setEmail("novo-membro@example.com");

        Contato contatoOwner = new Contato();
        contatoOwner.setId(301L);
        contatoOwner.setOwnerId(ownerId);
        contatoOwner.setEmail(owner.getEmail());
        contatoOwner.setIdContato(ownerId);
        contatoOwner.setPendente(Boolean.FALSE);

        Contato contatoExistente = new Contato();
        contatoExistente.setId(302L);
        contatoExistente.setOwnerId(ownerId);
        contatoExistente.setEmail("existente@example.com");
        contatoExistente.setIdContato(50L);
        contatoExistente.setPendente(Boolean.FALSE);

        Contato contatoNovo = new Contato();
        contatoNovo.setId(303L);
        contatoNovo.setOwnerId(ownerId);
        contatoNovo.setEmail("novo-membro@example.com");
        contatoNovo.setIdContato(novoUsuarioId);
        contatoNovo.setPendente(Boolean.FALSE);

        Grupo grupoExistente = new Grupo();
        grupoExistente.setId(grupoId);
        grupoExistente.setNome("Grupo Notificação");
        grupoExistente.setOwnerId(ownerId);
        grupoExistente.setMembros(new java.util.ArrayList<>(List.of(contatoOwner, contatoExistente)));
        grupoExistente.setAdminContatoId(contatoOwner.getId());

        Grupo request = new Grupo();
        request.setMembros(List.of(contatoExistente, contatoNovo));

        when(grupoRepository.findByIdAndOwnerId(grupoId, ownerId)).thenReturn(Optional.of(grupoExistente));
        when(usuarioRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(usuarioRepository.findById(novoUsuarioId)).thenReturn(Optional.of(usuarioNovo));
        when(contatoRepository.findByOwnerIdAndEmail(ownerId, owner.getEmail())).thenReturn(Optional.of(contatoOwner));
        when(contatoRepository.findByOwnerIdAndIdIn(eq(ownerId), anyList())).thenReturn(List.of(contatoExistente, contatoNovo));
        when(contatoRepository.findByOwnerIdAndIdContato(ownerId, contatoExistente.getIdContato()))
                .thenReturn(Optional.of(contatoExistente));
        when(contatoRepository.findByOwnerIdAndIdContato(ownerId, contatoNovo.getIdContato()))
                .thenReturn(Optional.of(contatoNovo));
        when(contatoRepository.findByOwnerIdAndEmailIgnoreCase(ownerId, "novo-membro@example.com"))
                .thenReturn(Optional.of(contatoNovo));
        when(grupoRepository.save(any(Grupo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificacaoConfiguracaoRepository.findByUsuarioId(novoUsuarioId)).thenReturn(Optional.of(configuracao(false)));

        grupoService.atualizarGrupo(grupoId, request, ownerId);

        verify(notificacaoService, never()).registrarNotificacao(anyLong(), any(NotificacaoLogRequest.class));
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
    @Test
    void sairDoGrupoReatribuiAdministradorAteUltimoMembro() {
        Long grupoId = 900L;
        Long joaoId = 200L;
        Long pedroId = 100L;
        Long pauloId = 400L;

        Usuario usuarioJoao = new Usuario();
        usuarioJoao.setId(joaoId);
        usuarioJoao.setEmail("joao@example.com");

        Usuario usuarioPedro = new Usuario();
        usuarioPedro.setId(pedroId);
        usuarioPedro.setEmail("pedro@example.com");

        Usuario usuarioPaulo = new Usuario();
        usuarioPaulo.setId(pauloId);
        usuarioPaulo.setEmail("paulo@example.com");

        Contato contatoJoao = new Contato();
        contatoJoao.setId(1L);
        contatoJoao.setOwnerId(joaoId);
        contatoJoao.setEmail("joao@example.com");
        contatoJoao.setIdContato(joaoId);

        Contato contatoPedro = new Contato();
        contatoPedro.setId(2L);
        contatoPedro.setOwnerId(joaoId);
        contatoPedro.setEmail("pedro@example.com");
        contatoPedro.setIdContato(pedroId);

        Contato contatoPaulo = new Contato();
        contatoPaulo.setId(3L);
        contatoPaulo.setOwnerId(joaoId);
        contatoPaulo.setEmail("paulo@example.com");
        contatoPaulo.setIdContato(pauloId);

        Grupo grupo = new Grupo();
        grupo.setId(grupoId);
        grupo.setNome("grupo A");
        grupo.setOwnerId(joaoId);
        grupo.addMembro(contatoJoao);
        grupo.addMembro(contatoPedro);
        grupo.addMembro(contatoPaulo);
        grupo.setAdminContatoId(contatoJoao.getId());

        when(grupoRepository.findByIdAndOwnerId(grupoId, joaoId)).thenReturn(Optional.of(grupo));
        when(grupoRepository.findByIdAndOwnerId(grupoId, pedroId)).thenReturn(Optional.of(grupo));
        when(grupoRepository.findByIdAndOwnerId(grupoId, pauloId)).thenReturn(Optional.of(grupo));
        when(usuarioRepository.findById(joaoId)).thenReturn(Optional.of(usuarioJoao));
        when(usuarioRepository.findById(pedroId)).thenReturn(Optional.of(usuarioPedro));
        when(usuarioRepository.findById(pauloId)).thenReturn(Optional.of(usuarioPaulo));
        when(grupoRepository.save(any(Grupo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        grupoService.sairDoGrupo(grupoId, joaoId);

        assertEquals(pedroId, grupo.getOwnerId());
        assertEquals(contatoPedro.getId(), grupo.getAdminContatoId());
        assertFalse(grupo.getMembros().contains(contatoJoao));

        grupoService.sairDoGrupo(grupoId, pedroId);

        assertEquals(pauloId, grupo.getOwnerId());
        assertEquals(contatoPaulo.getId(), grupo.getAdminContatoId());
        assertFalse(grupo.getMembros().contains(contatoPedro));
        assertTrue(grupo.getMembros().contains(contatoPaulo));

        grupoService.sairDoGrupo(grupoId, pauloId);

        assertTrue(grupo.getMembros().isEmpty());
        verify(grupoRepository).delete(grupo);
        verify(grupoRepository, times(2)).save(grupo);
    }

    private NotificacaoConfiguracao configuracao(boolean incluso) {
        NotificacaoConfiguracao configuracao = new NotificacaoConfiguracao();
        configuracao.setInclusoEmGrupo(incluso);
        return configuracao;
    }
}