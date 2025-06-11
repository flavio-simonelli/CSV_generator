package it.isw2.flaviosimonelli.utils;

import it.isw2.flaviosimonelli.model.Version;

import java.util.Comparator;

public class NameVersionComparator implements Comparator<String> {

    @Override
    public int compare(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int num1 = i < parts1.length ? parseIntSafe(parts1[i]) : 0;
            int num2 = i < parts2.length ? parseIntSafe(parts2[i]) : 0;

            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        return 0; // versioni uguali
    }

    public int compare(Version v1, Version v2) {
        return compare(v1.getName(), v2.getName());
    }

    private int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0; // fallback se il formato è strano
        }
    }
}