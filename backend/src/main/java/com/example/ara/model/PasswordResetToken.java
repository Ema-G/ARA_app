package com.example.ara.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private boolean used = false;

    public PasswordResetToken() {}

    public PasswordResetToken(String token, User user) {
        this.token = token;
        this.user = user;
        this.expiresAt = LocalDateTime.now().plusHours(1);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public Long getId()                       { return id; }
    public String getToken()                  { return token; }
    public void setToken(String token)        { this.token = token; }
    public User getUser()                     { return user; }
    public void setUser(User user)            { this.user = user; }
    public LocalDateTime getExpiresAt()       { return expiresAt; }
    public boolean isUsed()                   { return used; }
    public void setUsed(boolean used)         { this.used = used; }
}
