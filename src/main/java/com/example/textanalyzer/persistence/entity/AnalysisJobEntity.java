package com.example.textanalyzer.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "analysis_words", joinColumns = @JoinColumn(name = "analysis_id"))
    @OrderColumn(name = "position")
    private List<WordCountEmbeddable> words = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "analysis_errors", joinColumns = @JoinColumn(name = "analysis_id"))
    @OrderColumn(name = "position")
    private List<FileErrorEmbeddable> errors = new ArrayList<>();
}
