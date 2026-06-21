package com.example.textanalyzer.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "audit_logs")
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String action;

    private Instant createdAt;

    @Column(length = 2000)
    private String parameters;

    public AuditLogEntity(String username, String action, Instant createdAt, String parameters) {
        this.username = username;
        this.action = action;
        this.createdAt = createdAt;
        this.parameters = parameters;
    }
}