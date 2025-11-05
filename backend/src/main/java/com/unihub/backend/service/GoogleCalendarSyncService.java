package com.unihub.backend.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.unihub.backend.model.Avaliacao;
import com.unihub.backend.repository.AvaliacaoRepository;
import com.unihub.backend.repository.GoogleCalendarCredentialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GoogleCalendarSyncService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarSyncService.class);

    private final GoogleCalendarCredentialService credentialService;
    private final GoogleCalendarCredentialRepository credentialRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    public GoogleCalendarSyncService(GoogleCalendarCredentialService credentialService,
                                     GoogleCalendarCredentialRepository credentialRepository,
                                     AvaliacaoRepository avaliacaoRepository) {
        this.credentialService = credentialService;
        this.credentialRepository = credentialRepository;
        this.avaliacaoRepository = avaliacaoRepository;
    }

    @Transactional
    public SyncResult syncAll(Long usuarioId) {
        var clientOpt = credentialService.prepareCalendar(usuarioId);
        if (clientOpt.isEmpty()) {
            return new SyncResult(0, 0, null);
        }
        var client = clientOpt.get();
        List<Avaliacao> avaliacoes = avaliacaoRepository.findByUsuarioId(usuarioId);
        int success = 0;
        int failures = 0;
        for (Avaliacao avaliacao : avaliacoes) {
            if (syncSingle(avaliacao, client)) {
                success++;
            } else {
                failures++;
            }
        }
        Instant now = Instant.now();
        client.credential().setLastSyncedAt(now);
        credentialRepository.save(client.credential());
        return new SyncResult(success, failures, now);
    }

    @Transactional
    public void syncEvaluation(Long avaliacaoId, Long usuarioId) {
        var clientOpt = credentialService.prepareCalendar(usuarioId);
        if (clientOpt.isEmpty()) {
            return;
        }
        var client = clientOpt.get();
        avaliacaoRepository.findByIdAndUsuarioId(avaliacaoId, usuarioId).ifPresent(avaliacao -> {
            if (syncSingle(avaliacao, client)) {
                Instant now = Instant.now();
                client.credential().setLastSyncedAt(now);
                credentialRepository.save(client.credential());
            }
        });
    }

    @Transactional
    public void removeFromCalendar(Long avaliacaoId, Long usuarioId) {
        var clientOpt = credentialService.prepareCalendar(usuarioId);
        if (clientOpt.isEmpty()) {
            return;
        }
        var client = clientOpt.get();
        avaliacaoRepository.findByIdAndUsuarioId(avaliacaoId, usuarioId).ifPresent(avaliacao -> {
            String eventId = avaliacao.getGoogleCalendarEventId();
            if (eventId == null || eventId.isBlank()) {
                return;
            }
            try {
                client.calendar().events().delete("primary", eventId).execute();
            } catch (Exception e) {
                logger.warn("Falha ao excluir evento {} do Google Calendar", eventId, e);
            }
            avaliacao.setGoogleCalendarEventId(null);
            avaliacaoRepository.save(avaliacao);
            client.credential().setLastSyncedAt(Instant.now());
            credentialRepository.save(client.credential());
        });
    }

    @Transactional
    public void clearMetadata(Long usuarioId) {
        List<Avaliacao> avaliacoes = avaliacaoRepository.findByUsuarioId(usuarioId);
        boolean modified = false;
        for (Avaliacao avaliacao : avaliacoes) {
            if (avaliacao.getGoogleCalendarEventId() != null) {
                avaliacao.setGoogleCalendarEventId(null);
                modified = true;
            }
        }
        if (modified) {
            avaliacaoRepository.saveAll(avaliacoes);
        }
    }

    @Transactional
    public void removeAllRemoteEvents(Long usuarioId) {
        var clientOpt = credentialService.prepareCalendar(usuarioId);
        if (clientOpt.isEmpty()) {
            return;
        }
        var client = clientOpt.get();
        List<Avaliacao> avaliacoes = avaliacaoRepository.findByUsuarioId(usuarioId);
        for (Avaliacao avaliacao : avaliacoes) {
            String eventId = avaliacao.getGoogleCalendarEventId();
            if (eventId == null || eventId.isBlank()) {
                continue;
            }
            try {
                client.calendar().events().delete("primary", eventId).execute();
            } catch (Exception e) {
                logger.warn("Falha ao remover evento {} do Google Calendar durante desvinculação", eventId, e);
            }
        }
        client.credential().setLastSyncedAt(Instant.now());
        credentialRepository.save(client.credential());
    }

    private boolean syncSingle(Avaliacao avaliacao, GoogleCalendarCredentialService.CalendarClient client) {
        try {
            if (avaliacao.getDataEntrega() == null) {
                if (avaliacao.getGoogleCalendarEventId() != null) {
                    try {
                        client.calendar().events().delete("primary", avaliacao.getGoogleCalendarEventId()).execute();
                    } catch (Exception e) {
                        logger.warn("Falha ao remover evento do Google Calendar para avaliação {}", avaliacao.getId(), e);
                    }
                    avaliacao.setGoogleCalendarEventId(null);
                    avaliacaoRepository.save(avaliacao);
                }
                return true;
            }

            Event event = buildEvent(avaliacao);
            if (avaliacao.getGoogleCalendarEventId() == null || avaliacao.getGoogleCalendarEventId().isBlank()) {
                Event created = client.calendar().events().insert("primary", event).execute();
                avaliacao.setGoogleCalendarEventId(created.getId());
            } else {
                Event updated = client.calendar().events()
                        .update("primary", avaliacao.getGoogleCalendarEventId(), event)
                        .execute();
                avaliacao.setGoogleCalendarEventId(updated.getId());
            }
            avaliacaoRepository.save(avaliacao);
            return true;
        } catch (Exception e) {
            logger.warn("Falha ao sincronizar avaliação {} com o Google Calendar", avaliacao.getId(), e);
            return false;
        }
    }

    private Event buildEvent(Avaliacao avaliacao) {
        Event event = new Event();
        String summary = avaliacao.getDescricao();
        if (summary == null || summary.isBlank()) {
            summary = "Avaliação";
        }
        event.setSummary(summary);

        if (avaliacao.getDisciplina() != null && avaliacao.getDisciplina().getNome() != null) {
            event.setLocation(avaliacao.getDisciplina().getNome());
        }

        event.setDescription(buildDescription(avaliacao));

        LocalDateTime dataEntrega = avaliacao.getDataEntrega();
        if (dataEntrega != null) {
            event.setStart(toEventDateTime(dataEntrega));
            event.setEnd(toEventDateTime(dataEntrega.plusHours(1)));
        }
        return event;
    }

    private EventDateTime toEventDateTime(LocalDateTime dateTime) {
        ZonedDateTime zoned = dateTime.atZone(ZoneId.systemDefault());
        return new EventDateTime()
                .setDateTime(new DateTime(zoned.toInstant().toEpochMilli()))
                .setTimeZone(zoned.getZone().getId());
    }

    private String buildDescription(Avaliacao avaliacao) {
        StringBuilder sb = new StringBuilder();
        if (avaliacao.getTipoAvaliacao() != null && !avaliacao.getTipoAvaliacao().isBlank()) {
            sb.append("Tipo: ").append(avaliacao.getTipoAvaliacao()).append("\n");
        }
        if (avaliacao.getPrioridade() != null) {
            sb.append("Prioridade: ").append(avaliacao.getPrioridade()).append("\n");
        }
        if (avaliacao.getEstado() != null) {
            sb.append("Status: ").append(avaliacao.getEstado()).append("\n");
        }
        if (avaliacao.getModalidade() != null) {
            sb.append("Modalidade: ").append(avaliacao.getModalidade()).append("\n");
        }
        if (avaliacao.getIntegrantes() != null && !avaliacao.getIntegrantes().isEmpty()) {
            String integrantes = avaliacao.getIntegrantes().stream()
                    .map(contato -> contato.getNome() != null ? contato.getNome() : contato.getEmail())
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));
            if (!integrantes.isBlank()) {
                sb.append("Integrantes: ").append(integrantes).append("\n");
            }
        }
        if (avaliacao.getNota() != null) {
            sb.append("Nota: ").append(avaliacao.getNota()).append("\n");
        }
        if (avaliacao.getPeso() != null) {
            sb.append("Peso: ").append(avaliacao.getPeso()).append("\n");
        }
        if (avaliacao.getDisciplina() != null && avaliacao.getDisciplina().getNome() != null) {
            sb.append("Disciplina: ").append(avaliacao.getDisciplina().getNome()).append("\n");
        }
        return sb.toString().trim();
    }

    public record SyncResult(int synced, int failures, Instant lastSyncedAt) {}
}