package com.dtoan.project.fptassetmanagement.util;

import java.text.Normalizer;
import java.util.Locale;

public final class CodeNormalizer {

    private CodeNormalizer() {
    }

    public static String normalizeRoomCode(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    public static String asciiToken(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('Đ', 'D')
                .replace('đ', 'd')
                .replaceAll("[^A-Za-z0-9]+", "")
                .toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return "";
        }
        return normalized.substring(0, Math.min(maxLength, normalized.length()));
    }
}
