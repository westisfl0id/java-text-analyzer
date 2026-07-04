package com.example.textanalyzer.cli;

/**
 * Supported execution modes for text-file analysis.
 */
public enum AnalysisMode {
    SINGLE,
    MULTI;

    /**
     * Converts a user-provided mode name to an enum value.
     *
     * @param value raw mode name
     * @return parsed analysis mode
     * @throws IllegalArgumentException when the value is not supported
     */
    public static AnalysisMode fromString(String value) {
        return switch (value.toLowerCase()) {
            case "single" -> SINGLE;
            case "multi" -> MULTI;
            default -> throw new IllegalArgumentException("Unsupported analysis mode: " + value);
        };
    }

    /**
     * Returns the lowercase value used in CLI options and persisted job data.
     *
     * @return mode name suitable for external output
     */
    public String cliValue() {
        return name().toLowerCase();
    }
}
