package it.isw2.flaviosimonelli.utils;

public class VersionTagger {
    public static String applyVersion(String template, String version) {
        if (template == null || template.isEmpty()) {
            throw new IllegalArgumentException("Template cannot be null or empty.");
        }
        return template.replace("{VERSION}", version);
    }
}

