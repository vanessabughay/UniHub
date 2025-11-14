package com.unihub.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unihub.backend.dto.planejamento.QuadroPlanejamentoRequest;
import com.unihub.backend.model.Contato;
import com.unihub.backend.model.Notificacao;
import com.unihub.backend.model.NotificacaoConfiguracao;
import com.unihub.backend.model.QuadroPlanejamento;
import com.unihub.backend.model.Usuario;
import com.unihub.backend.repository.ColunaPlanejamentoRepository;
import com.unihub.backend.repository.ContatoRepository;
import com.unihub.backend.repository.DisciplinaRepository;
import com.unihub.backend.repository.GrupoRepository;
import com.unihub.backend.repository.NotificacaoConfiguracaoRepository;
import com.unihub.backend.repository.NotificacaoRepository;
import com.unihub.backend.repository.QuadroPlanejamentoRepository;
import com.unihub.backend.repository.TarefaComentarioRepository;
import com.unihub.backend.repository.TarefaNotificacaoRepository;
import com.unihub.backend.repository.TarefaPlanejamentoRepository;
import com.unihub.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuadroPlanejamentoServiceTest {

    @Mock
    private QuadroPlanejamentoRepository quadroPlanejamentoRepository;
    @Mock
    private ColunaPlanejamentoRepository colunaPlanejamentoRepository;
    @Mock
    private TarefaPlanejamentoRepository tarefaPlanejamentoRepository;
    @Mock
    private ContatoRepository contatoRepository;
    @Mock
    private GrupoRepository grupoRepository;
    @Mock
    private DisciplinaRepository disciplinaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private TarefaComentarioRepository tarefaComentarioRepository;
    @Mock
    private TarefaNotificacaoRepository tarefaNotificacaoRepository;
    @Mock
    private NotificacaoRepository notificacaoRepository;
    @Mock
    private NotificacaoConfiguracaoRepository notificacaoConfiguracaoRepository;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private QuadroPlanejamentoService quadroPlanejamentoService;

    @Test
    void criarQuadroComContatoDisparaNotificacaoParaNovoParticipante() throws Exception {
        Long ownerId = 1L;
        Long contatoChave = 10L;
        Long participanteId = 2L;

        Usuario owner = new Usuario();
        owner.setId(ownerId);
        owner.setNomeUsuario("Owner");

        Usuario participante = new Usuario();
        participante.setId(participanteId);
        participante.setNomeUsuario("Participante");

        Contato contato = new Contato();
        contato.setIdContato(participanteId);

        QuadroPlanejamentoRequest request = new QuadroPlanejamentoRequest();
        request.setNome("Quadro Teste");
        request.setContatoId(contatoChave);

        when(usuarioRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(usuarioRepository.findById(participanteId)).thenReturn(Optional.of(participante));
        when(contatoRepository.findByOwnerIdAndIdContato(ownerId, contatoChave)).thenReturn(Optional.of(contato));

        when(notificacaoConfiguracaoRepository.findByUsuarioId(participanteId))
                .thenReturn(Optional.of(criarConfiguracao(true)));
        when(notificacaoRepository.findByUsuarioIdAndTipoAndCategoriaAndReferenciaId(eq(participanteId),
                eq("APP_NOTIFICACAO"), eq("QUADRO_PLANEJAMENTO"), any(Long.class)))
                .thenReturn(Optional.empty());
        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{\"action\":\"OPEN_QUADRO\",\"quadroId\":55}");
        when(notificacaoRepository.save(any(Notificacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(quadroPlanejamentoRepository.save(any(QuadroPlanejamento.class)))
                .thenAnswer(invocation -> {
                    QuadroPlanejamento quadro = invocation.getArgument(0);
                    quadro.setId(55L);
                    return quadro;
                });

        quadroPlanejamentoService.criar(request, ownerId);

        ArgumentCaptor<Notificacao> captor = ArgumentCaptor.forClass(Notificacao.class);
        verify(notificacaoRepository).save(captor.capture());
        Notificacao notificacaoSalva = captor.getValue();

        assertEquals(participanteId, notificacaoSalva.getUsuario().getId());
        assertEquals(55L, notificacaoSalva.getReferenciaId());
        assertEquals("APP_NOTIFICACAO", notificacaoSalva.getTipo());
        assertEquals("QUADRO_PLANEJAMENTO", notificacaoSalva.getCategoria());
        assertEquals("Quadro compartilhado", notificacaoSalva.getTitulo());
        assertEquals("Você foi adicionado ao quadro \"Quadro Teste\".", notificacaoSalva.getMensagem());
        assertEquals("{\"action\":\"OPEN_QUADRO\",\"quadroId\":55}", notificacaoSalva.getMetadataJson());
        assertNotNull(notificacaoSalva.getCriadaEm());
    }

    @Test
    void atualizarQuadroRemovendoContatoNaoDisparaNovasNotificacoes() {
        Long quadroId = 90L;
        Long ownerId = 5L;
        Long participanteId = 7L;

        Usuario owner = new Usuario();
        owner.setId(ownerId);

        Contato contato = new Contato();
        contato.setIdContato(participanteId);

        QuadroPlanejamento existente = new QuadroPlanejamento();
        existente.setId(quadroId);
        existente.setUsuario(owner);
        existente.setTitulo("Quadro Original");
        existente.setContato(contato);

        when(quadroPlanejamentoRepository.findByIdAndUsuarioHasAccess(quadroId, ownerId))
                .thenReturn(Optional.of(existente));
        when(quadroPlanejamentoRepository.save(any(QuadroPlanejamento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        QuadroPlanejamentoRequest request = new QuadroPlanejamentoRequest();
        request.setNome("Quadro Original");

        quadroPlanejamentoService.atualizar(quadroId, request, ownerId);

        verifyNoInteractions(notificacaoRepository);
        verifyNoInteractions(notificacaoConfiguracaoRepository);
    }

    private NotificacaoConfiguracao criarConfiguracao(boolean incluir) {
        NotificacaoConfiguracao configuracao = new NotificacaoConfiguracao();
        configuracao.setIncluirEmQuadro(incluir);
        return configuracao;
    }
}