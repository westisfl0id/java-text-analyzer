package com.example.textanalyzer.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Database entity that stores one text-analysis job and its calculated result.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "analyses")
public class AnalysisJobEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String directory;

    private int minWordLength;

    private int topCount;

    private String mode;

    private int threads;

    private int processedFiles;

    private long executionTimeMs;

    @Enumerated(EnumType.STRING)
    private AnalysisStatus status;

    private String createdBy;

    private Instant createdAt;

    private Instant startedAt;

    private Instant finishedAt;

    @Column(length = 2000)
    private String errorMessage;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "analysis_words", joinColumns = @JoinColumn(name = "analysis_id"))
    @OrderColumn(name = "position")
    private List<WordCountEmbeddable> words = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "analysis_errors", joinColumns = @JoinColumn(name = "analysis_id"))
    @OrderColumn(name = "position")
    private List<FileErrorEmbeddable> errors = new ArrayList<>();
}