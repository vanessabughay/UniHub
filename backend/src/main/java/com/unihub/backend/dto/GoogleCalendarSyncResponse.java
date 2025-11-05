package com.unihub.backend.dto;

import java.time.Instant;

public record GoogleCalendarSyncResponse(
        int synced,
        int failures,
        Instant lastSyncedAt
) {}