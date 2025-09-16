package com.unihub.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class PasswordResetToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;                     
    @Column(length = 64, unique = true)
    private String tokenHash;                
    private Instant expiresAt;
    private Instant usedAt;

    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    public boolean isUsed() { return usedAt != null; }

    public Long getId() {
    return id;
}

public void setId(Long id) {
    this.id = id;
}

public Long getUserId() {
    return userId;
}

public void setUserId(Long userId) {
    this.userId = userId;
}

public String getTokenHash() {
    return tokenHash;
}

public void setTokenHash(String tokenHash) {
    this.tokenHash = tokenHash;
}

public Instant getExpiresAt() {
    return expiresAt;
}

public void setExpiresAt(Instant expiresAt) {
    this.expiresAt = expiresAt;
}

public Instant getUsedAt() {
    return usedAt;
}

public void setUsedAt(Instant usedAt) {
    this.usedAt = usedAt;
}

}
