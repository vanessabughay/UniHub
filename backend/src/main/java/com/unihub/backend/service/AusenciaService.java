package com.unihub.backend.service;

import com.unihub.backend.dto.notificacoes.NotificacaoLogRequest;
import com.unihub.backend.model.Ausencia;
import com.unihub.backend.model.Disciplina;
import com.unihub.backend.model.NotificacaoConfiguracao;
import com.unihub.backend.model.Usuario;
import com.unihub.backend.repository.AusenciaRepository;
import com.unihub.backend.repository.DisciplinaRepository;
import com.unihub.backend.repository.NotificacaoConfiguracaoRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AusenciaService {
    
    private static final String NOTIFICACAO_CATEGORIA_PRESENCA = "PRESENCA";
    private static final String NOTIFICACAO_TIPO_PRESENCA = "PRESENCA_AULA";
    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            .withLocale(new Locale("pt", "BR"));

    private final AusenciaRepository repository;
    private final CategoriaService categoriaService;
    private final DisciplinaRepository disciplinaRepository;
    private final NotificacaoService notificacaoService;
    private final NotificacaoConfiguracaoRepository notificacaoConfiguracaoRepository;

     public AusenciaService(AusenciaRepository repository,
                           CategoriaService categoriaService,
                           DisciplinaRepository disciplinaRepository,
                           NotificacaoService notificacaoService,
                           NotificacaoConfiguracaoRepository notificacaoConfiguracaoRepository) {
        this.repository = repository;
        this.categoriaService = categoriaService;
        this.disciplinaRepository = disciplinaRepository;
        this.notificacaoService = notificacaoService;
        this.notificacaoConfiguracaoRepository = notificacaoConfiguracaoRepository;
    }

    public List<Ausencia> listarTodas(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    public Ausencia salvar(Ausencia ausencia, Long usuarioId) {
          boolean novaAusencia = ausencia.getId() == null;
        if (ausencia.getCategoria() != null && !ausencia.getCategoria().isBlank()) {
            categoriaService.buscarOuCriar(ausencia.getCategoria(), usuarioId);
        }
        if (ausencia.getDisciplinaId() != null) {
            Disciplina disciplina = disciplinaRepository.findByIdAndUsuarioId(ausencia.getDisciplinaId(), usuarioId)
                    .orElseThrow(() -> new RuntimeException("Disciplina com ID " + ausencia.getDisciplinaId() + " não encontrada para este usuário."));
            ausencia.setDisciplina(disciplina);
        }
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        ausencia.setUsuario(usuario);
        Ausencia salvo = repository.save(ausencia);
        registrarNotificacaoAusencia(usuarioId, salvo, novaAusencia);
        return salvo;
    }

    public Ausencia buscarPorId(Long id, Long usuarioId) {
        return repository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("Ausência não encontrada"));
    }

    public void excluir(Long id, Long usuarioId) {
        Ausencia ausencia = buscarPorId(id, usuarioId);
        repository.delete(ausencia);
    }
    
    private void registrarNotificacaoAusencia(Long usuarioId, Ausencia ausencia, boolean novaAusencia) {
        if (usuarioId == null || ausencia == null) {
            return;
        }

        boolean desejaNotificar = notificacaoConfiguracaoRepository.findByUsuarioId(usuarioId)
                .map(NotificacaoConfiguracao::isNotificacaoDePresenca)
                .orElse(true);

        if (!desejaNotificar) {
            return;
        }

        String mensagem = montarMensagemAusencia(ausencia, novaAusencia);
        if (mensagem == null || mensagem.isBlank()) {
            return;
        }

        NotificacaoLogRequest request = new NotificacaoLogRequest();
        request.setCategoria(NOTIFICACAO_CATEGORIA_PRESENCA);
        request.setTipo(NOTIFICACAO_TIPO_PRESENCA);
        request.setTitulo(novaAusencia ? "Ausência registrada" : "Ausência atualizada");
        request.setMensagem(mensagem);
        request.setReferenciaId(ausencia.getId());
        request.setInteracaoPendente(false);
        request.setTimestamp(System.currentTimeMillis());

        Map<String, Object> metadata = new LinkedHashMap<>();
        Long disciplinaId = ausencia.getDisciplinaId();
        if (disciplinaId == null && ausencia.getDisciplina() != null) {
            disciplinaId = ausencia.getDisciplina().getId();
        }
        if (disciplinaId != null) {
            metadata.put("disciplinaId", disciplinaId);
        }
        if (ausencia.getDisciplina() != null && ausencia.getDisciplina().getNome() != null) {
            metadata.put("disciplinaNome", ausencia.getDisciplina().getNome());
        }
        if (ausencia.getId() != null) {
            metadata.put("ausenciaId", ausencia.getId());
        }
        if (ausencia.getData() != null) {
            metadata.put("data", ausencia.getData().toString());
        }
        if (ausencia.getCategoria() != null && !ausencia.getCategoria().isBlank()) {
            metadata.put("categoriaAusencia", ausencia.getCategoria());
        }
        metadata.put("response", "ABSENCE");
        metadata.put("operation", novaAusencia ? "CREATE" : "UPDATE");

        if (!metadata.isEmpty()) {
            request.setMetadata(metadata);
        }

        notificacaoService.registrarNotificacao(usuarioId, request);
    }

    private String montarMensagemAusencia(Ausencia ausencia, boolean novaAusencia) {
        Disciplina disciplina = ausencia.getDisciplina();
        String nomeDisciplina = disciplina != null ? disciplina.getNome() : null;
        String dataFormatada = ausencia.getData() != null ? DATA_FORMATTER.format(ausencia.getData()) : null;

        if (nomeDisciplina == null && dataFormatada == null) {
            return novaAusencia ? "Ausência registrada." : "Ausência atualizada.";
        }

        if (nomeDisciplina != null && dataFormatada != null) {
            return novaAusencia
                    ? String.format("Ausência registrada em %s no dia %s.", nomeDisciplina, dataFormatada)
                    : String.format("Ausência em %s no dia %s foi atualizada.", nomeDisciplina, dataFormatada);
        }

        if (nomeDisciplina != null) {
            return novaAusencia
                    ? String.format("Ausência registrada em %s.", nomeDisciplina)
                    : String.format("Ausência em %s foi atualizada.", nomeDisciplina);
        }

        return novaAusencia
                ? String.format("Ausência registrada para %s.", dataFormatada)
                : String.format("Ausência do dia %s foi atualizada.", dataFormatada);
    }
}