package com.unihub.backend.controller;

import com.unihub.backend.dto.GoogleCalendarLinkRequest;
import com.unihub.backend.dto.GoogleCalendarStatusResponse;
import com.unihub.backend.dto.GoogleCalendarSyncResponse;
import com.unihub.backend.service.GoogleCalendarCredentialService;
import com.unihub.backend.service.GoogleCalendarSyncService;
import com.unihub.backend.service.GoogleCalendarSyncService.SyncResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/google/calendar")
public class GoogleCalendarController {

    private final GoogleCalendarCredentialService credentialService;
    private final GoogleCalendarSyncService syncService;

    public GoogleCalendarController(GoogleCalendarCredentialService credentialService,
                                    GoogleCalendarSyncService syncService) {
        this.credentialService = credentialService;
        this.syncService = syncService;
    }

    @GetMapping("/status")
    public ResponseEntity<GoogleCalendarStatusResponse> status(@AuthenticationPrincipal Long usuarioId) {
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var credentialOpt = credentialService.findByUsuario(usuarioId);
        if (credentialOpt.isEmpty()) {
            return ResponseEntity.ok(new GoogleCalendarStatusResponse(false, null, false));
        }
        var credential = credentialOpt.get();
        boolean requiresReauth = credential.getRefreshToken() == null || credential.getRefreshToken().isBlank();
        return ResponseEntity.ok(new GoogleCalendarStatusResponse(true, credential.getLastSyncedAt(), requiresReauth));
    }

    @PostMapping("/link")
    public ResponseEntity<GoogleCalendarStatusResponse> link(@AuthenticationPrincipal Long usuarioId,
                                                             @RequestBody GoogleCalendarLinkRequest request) {
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (request == null || request.authCode() == null || request.authCode().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            var credential = credentialService.link(usuarioId, request.authCode());
            SyncResult syncResult = syncService.syncAll(usuarioId);
            boolean requiresReauth = credential.getRefreshToken() == null || credential.getRefreshToken().isBlank();
            return ResponseEntity.ok(new GoogleCalendarStatusResponse(true, syncResult.lastSyncedAt(), requiresReauth));
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @DeleteMapping("/link")
    public ResponseEntity<GoogleCalendarStatusResponse> unlink(@AuthenticationPrincipal Long usuarioId) {
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        syncService.removeAllRemoteEvents(usuarioId);
        credentialService.unlink(usuarioId);
        syncService.clearMetadata(usuarioId);
        return ResponseEntity.ok(new GoogleCalendarStatusResponse(false, null, false));
    }

    @PostMapping("/sync")
    public ResponseEntity<GoogleCalendarSyncResponse> sync(@AuthenticationPrincipal Long usuarioId) {
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        SyncResult result = syncService.syncAll(usuarioId);
        return ResponseEntity.ok(new GoogleCalendarSyncResponse(result.synced(), result.failures(), result.lastSyncedAt()));
    }
}