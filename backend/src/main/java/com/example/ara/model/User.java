package com.example.ara.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ── UserDetails ──────────────────────────────────────────────
    @Override public String getUsername() { return email; }
    @Override public String getPassword() { return password; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }

    // ── Getters / Setters ────────────────────────────────────────
    public Long getId()                    { return id; }
    public void setId(Long id)             { this.id = id; }
    public String getEmail()               { return email; }
    public void setEmail(String email)     { this.email = email; }
    public void setPassword(String p)      { this.password = p; }
    public String getFullName()            { return fullName; }
    public void setFullName(String n)      { this.fullName = n; }
    public LocalDateTime getCreatedAt()    { return createdAt; }
}
