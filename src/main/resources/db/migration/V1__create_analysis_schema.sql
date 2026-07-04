CREATE TABLE analyses (
                          id BIGSERIAL PRIMARY KEY,
                          directory VARCHAR(1024) NOT NULL,
                          min_word_length INTEGER NOT NULL,
                          top_count INTEGER NOT NULL,
                          mode VARCHAR(16) NOT NULL,
                          threads INTEGER NOT NULL,
                          processed_files INTEGER NOT NULL DEFAULT 0,
                          execution_time_ms BIGINT NOT NULL DEFAULT 0,
                          status VARCHAR(32) NOT NULL,
                          created_by VARCHAR(255) NOT NULL,
                          created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                          started_at TIMESTAMP WITH TIME ZONE,
                          finished_at TIMESTAMP WITH TIME ZONE,
                          error_message VARCHAR(2000)
);

CREATE INDEX idx_analyses_created_at ON analyses (created_at DESC);
CREATE INDEX idx_analyses_status ON analyses (status);
CREATE INDEX idx_analyses_created_by ON analyses (created_by);

CREATE TABLE analysis_words (
                                analysis_id BIGINT NOT NULL REFERENCES analyses (id) ON DELETE CASCADE,
                                position INTEGER NOT NULL,
                                word VARCHAR(255) NOT NULL,
                                count_value BIGINT NOT NULL,
                                PRIMARY KEY (analysis_id, position)
);

CREATE INDEX idx_analysis_words_word ON analysis_words (word);
CREATE INDEX idx_analysis_words_count_value ON analysis_words (count_value DESC);

CREATE TABLE analysis_errors (
                                 analysis_id BIGINT NOT NULL REFERENCES analyses (id) ON DELETE CASCADE,
                                 position INTEGER NOT NULL,
                                 file VARCHAR(1024) NOT NULL,
                                 message VARCHAR(2000) NOT NULL,
                                 PRIMARY KEY (analysis_id, position)
);

CREATE TABLE audit_logs (
                            id BIGSERIAL PRIMARY KEY,
                            username VARCHAR(255) NOT NULL,
                            action VARCHAR(64) NOT NULL,
                            created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                            parameters VARCHAR(2000)
);

CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at DESC);
CREATE INDEX idx_audit_logs_username ON audit_logs (username);
CREATE INDEX idx_audit_logs_action ON audit_logs (action);