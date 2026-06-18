package com.example.textanalyzer.cli;

public enum AnalysisMode {
    SINGLE,
    MULTI;

    public static AnalysisMode fromString(String value) {
        return switch (value.toLowerCase()) {
            case "single" -> SINGLE;
            case "multi" -> MULTI;
            default -> throw new IllegalArgumentException("Unsupported analysis mode: " + value);
        };
    }

    public String cliValue() {
        return name().toLowerCase();
    }
}
