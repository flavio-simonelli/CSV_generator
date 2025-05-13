package it.isw2.flaviosimonelli.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import it.isw2.flaviosimonelli.model.Version;


public class CsvExporter {

    public static void writeVersionsToCsv(List<Version> versions, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Scrive l'intestazione
            writer.append("id,name,archived,released,releaseDate\n");

            // Scrive i dati
            for (Version version : versions) {
                writer.append(version.getId()).append(",")
                        .append(version.getName()).append(",")
                        .append(Boolean.toString(version.isArchived())).append(",")
                        .append(Boolean.toString(version.isReleased())).append(",")
                        .append(version.getReleaseDate() != null ? version.getReleaseDate().toString() : "")
                        .append("\n");
            }
            System.out.println("CSV scritto con successo: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
