package com.unihub.backend.service;

import com.unihub.backend.dto.notificacoes.AvaliacoesConfigDto;
import com.unihub.backend.dto.notificacoes.NotificacoesConfigRequest;
import com.unihub.backend.dto.notificacoes.NotificacoesConfigResponse;
import com.unihub.backend.model.NotificacaoConfiguracao;
import com.unihub.backend.model.NotificacaoConfiguracaoAntecedencia;
import com.unihub.backend.model.Usuario;
import com.unihub.backend.model.enums.Antecedencia;
import com.unihub.backend.model.enums.Prioridade;
import com.unihub.backend.repository.NotificacaoConfiguracaoRepository;
import com.unihub.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;

@Service
public class NotificacaoConfiguracaoService {

    private final NotificacaoConfiguracaoRepository configuracaoRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacaoConfiguracaoService(NotificacaoConfiguracaoRepository configuracaoRepository,
                                          UsuarioRepository usuarioRepository) {
        this.configuracaoRepository = configuracaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public NotificacoesConfigResponse carregar(Long usuarioId) {
        NotificacaoConfiguracao configuracao = obterOuCriarPadrao(usuarioId);
        return toResponse(configuracao);
    }

    @Transactional
    public NotificacoesConfigResponse salvar(Long usuarioId, NotificacoesConfigRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Configuração de notificações é obrigatória");
        }

        NotificacaoConfiguracao configuracao = obterOuCriarPadrao(usuarioId);

    
        configuracao.setNotificacaoDePresenca(Boolean.TRUE.equals(request.getNotificacaoDePresenca()));
        configuracao.setAvaliacoesAtivas(Boolean.TRUE.equals(request.getAvaliacoesAtivas()));
        configuracao.setCompartilhamentoDisciplina(Boolean.TRUE.equals(request.getCompartilhamentoDisciplina()));
        configuracao.setIncluirEmQuadro(Boolean.TRUE.equals(request.getIncluirEmQuadro()));
        configuracao.setPrazoTarefa(Boolean.TRUE.equals(request.getPrazoTarefa()));
        configuracao.setComentarioTarefa(Boolean.TRUE.equals(request.getComentarioTarefa()));
        configuracao.setConviteContato(Boolean.TRUE.equals(request.getConviteContato()));
        configuracao.setInclusoEmGrupo(Boolean.TRUE.equals(request.getInclusoEmGrupo()));
    

        atualizarAntecedencias(configuracao, request.getAvaliacoesConfig());

        NotificacaoConfiguracao salvo = configuracaoRepository.save(configuracao);
        return toResponse(salvo);
    }

    private NotificacaoConfiguracao obterOuCriarPadrao(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("Usuário autenticado é obrigatório");
        }

        return configuracaoRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> criarConfiguracaoPadrao(usuarioId));
    }

    private NotificacaoConfiguracao criarConfiguracaoPadrao(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        NotificacaoConfiguracao configuracao = new NotificacaoConfiguracao();
        configuracao.setUsuario(usuario);
        
        // Define os padrões (true) para todos os campos na criação
        configuracao.setNotificacaoDePresenca(true);
        configuracao.setAvaliacoesAtivas(true);
        configuracao.setCompartilhamentoDisciplina(true);
        configuracao.setIncluirEmQuadro(true);
        configuracao.setPrazoTarefa(true);
        configuracao.setComentarioTarefa(true);
        configuracao.setConviteContato(true);
        configuracao.setInclusoEmGrupo(true);


        for (Prioridade prioridade : Prioridade.values()) {
            NotificacaoConfiguracaoAntecedencia antecedencia = new NotificacaoConfiguracaoAntecedencia();
            antecedencia.setPrioridade(prioridade);
            antecedencia.setAntecedencia(Antecedencia.padrao());
            configuracao.addAntecedencia(antecedencia);
        }

        return configuracaoRepository.save(configuracao);
    }

    private void atualizarAntecedencias(NotificacaoConfiguracao configuracao, AvaliacoesConfigDto dto) {
         EnumMap<Prioridade, Antecedencia> valores = new EnumMap<>(Prioridade.class);
        if (dto != null && dto.getAntecedencia() != null) {
            valores.putAll(dto.getAntecedencia());
        }

        Map<Prioridade, NotificacaoConfiguracaoAntecedencia> existentes = configuracao.getAntecedencias()
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        NotificacaoConfiguracaoAntecedencia::getPrioridade,
                        entry -> entry,
                        (a, b) -> a,
                        () -> new EnumMap<>(Prioridade.class)
                ));

        for (Prioridade prioridade : Prioridade.values()) {
            Antecedencia antecedencia = valores.get(prioridade);
            if (antecedencia == null) {
                antecedencia = Antecedencia.padrao();
            }
            NotificacaoConfiguracaoAntecedencia registro = existentes.get(prioridade);
            if (registro == null) {
                registro = new NotificacaoConfiguracaoAntecedencia();
                registro.setPrioridade(prioridade);
                configuracao.addAntecedencia(registro);
            }
            registro.setAntecedencia(antecedencia);
        }
    }

    private NotificacoesConfigResponse toResponse(NotificacaoConfiguracao configuracao) {
        NotificacoesConfigResponse response = new NotificacoesConfigResponse();
        response.setNotificacaoDePresenca(configuracao.isNotificacaoDePresenca());
        response.setAvaliacoesAtivas(configuracao.isAvaliacoesAtivas());
        response.setCompartilhamentoDisciplina(configuracao.isCompartilhamentoDisciplina());
        response.setIncluirEmQuadro(configuracao.isIncluirEmQuadro());
        response.setPrazoTarefa(configuracao.isPrazoTarefa());
        response.setComentarioTarefa(configuracao.isComentarioTarefa());
        response.setConviteContato(configuracao.isConviteContato());
        response.setInclusoEmGrupo(configuracao.isInclusoEmGrupo());

        AvaliacoesConfigDto avaliacoesConfig = new AvaliacoesConfigDto();
        Map<Prioridade, Antecedencia> antecedencias = new EnumMap<>(Prioridade.class);

        configuracao.getAntecedencias().forEach(registro ->
                antecedencias.put(registro.getPrioridade(), registro.getAntecedencia())
        );

        for (Prioridade prioridade : Prioridade.values()) {
            antecedencias.putIfAbsent(prioridade, Antecedencia.padrao());
        }

        avaliacoesConfig.setAntecedencia(antecedencias);
        response.setAvaliacoesConfig(avaliacoesConfig);
        return response;
    }
}