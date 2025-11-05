package com.unihub.backend.dto;

import java.time.Instant;

public record GoogleCalendarStatusResponse(
        boolean linked,
        Instant lastSyncedAt,
        boolean requiresReauth
) {}