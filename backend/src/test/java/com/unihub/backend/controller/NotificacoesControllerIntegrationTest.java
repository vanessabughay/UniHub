package com.unihub.backend.controller;

import com.unihub.backend.TestMailConfiguration;
import com.unihub.backend.config.TestPropertiesConfig;
import com.unihub.backend.model.AuthToken;
import com.unihub.backend.model.Notificacao;
import com.unihub.backend.model.Usuario;
import com.unihub.backend.repository.AuthTokenRepository;
import com.unihub.backend.repository.NotificacaoRepository;
import com.unihub.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({ TestMailConfiguration.class, TestPropertiesConfig.class })
@TestPropertySource(properties = {
        "spring.mail.username=test",
        "spring.mail.password=test"
})
class NotificacoesControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private AuthTokenRepository authTokenRepository;

    @AfterEach
    void tearDown() {
        notificacaoRepository.deleteAll();
        authTokenRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void historicoSemTokenRetornaNaoAutorizado() throws Exception {
    mockMvc.perform(get("/api/notificacoes/historico"))
            .andExpect(status().is4xxClientError());
}


    @Test
    void historicoComTokenValidoRetornaNotificacoesDoMesmoUsuario() throws Exception {
        Usuario usuario = salvarUsuario("usuario1@example.com");
        String token = salvarToken(usuario);
        salvarNotificacao(usuario, "Primeira mensagem");

        mockMvc.perform(get("/api/notificacoes/usuarios/{usuarioId}/historico", usuario.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mensagem").value("Primeira mensagem"));
    }

    @Test
    void historicoComTokenValidoMasUsuarioDiferenteRetornaForbidden() throws Exception {
        Usuario usuarioAutenticado = salvarUsuario("usuario2@example.com");
        Usuario outroUsuario = salvarUsuario("usuario3@example.com");
        String token = salvarToken(usuarioAutenticado);

        mockMvc.perform(get("/api/notificacoes/usuarios/{usuarioId}/historico", outroUsuario.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private Usuario salvarUsuario(String email) {
        Usuario usuario = new Usuario();
        usuario.setNomeUsuario(email);
        usuario.setEmail(email);
        usuario.setSenha("password");
        Usuario salvo = usuarioRepository.save(usuario);
        assertThat(salvo.getId()).isNotNull();
        return salvo;
    }

    private String salvarToken(Usuario usuario) {
        AuthToken authToken = new AuthToken();
        authToken.setUsuarioId(usuario.getId());
        authToken.setToken(UUID.randomUUID().toString());
        authToken.setCriadoEm(LocalDateTime.now());
        authToken.setExpiraEm(LocalDateTime.now().plusDays(1));
        AuthToken salvo = authTokenRepository.save(authToken);
        assertThat(salvo.getToken()).isNotBlank();
        return salvo.getToken();
    }

    private void salvarNotificacao(Usuario usuario, String mensagem) {
        Notificacao notificacao = new Notificacao();
        notificacao.setUsuario(usuario);
        notificacao.setMensagem(mensagem);
        notificacao.setTitulo("Titulo");
        notificacao.setTipo("INFO");
        notificacao.setCategoria("GERAL");
        notificacao.setInteracaoPendente(false);
        notificacao.setLida(false);
        notificacao.setCriadaEm(LocalDateTime.now());
        notificacao.setAtualizadaEm(LocalDateTime.now());
        notificacaoRepository.save(notificacao);
    }
}
