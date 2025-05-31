package it.isw2.flaviosimonelli.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import it.isw2.flaviosimonelli.model.method.Method;
import it.isw2.flaviosimonelli.model.Project.Project;
import it.isw2.flaviosimonelli.model.Version;
import it.isw2.flaviosimonelli.model.Ticket;

public class CsvExporter {

    private static String escapeCsv(String field) {
        if (field == null) return "";
        String escaped = field.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    public static void writeVersionsToCsv(List<Version> versions, String filePath) {
        ensureParentDirectoryExists(filePath);

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("name,released,commit\n");

            for (Version version : versions) {
                writer.append(escapeCsv(version.getName())).append(",")
                        .append(Boolean.toString(version.isReleased())).append(",")
                        .append(escapeCsv(version.getHashCommit() != null ? version.getHashCommit() : ""))
                        .append("\n");
            }
            System.out.println("CSV scritto con successo: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeTicketsToCsv(List<Ticket> tickets, String filePath) {
        ensureParentDirectoryExists(filePath);

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("fixVersion,affectedVersion\n");

            for (Ticket ticket : tickets) {
                writer.append(escapeCsv(ticket.getFixVersion() != null ? ticket.getFixVersion().getName() : "")).append(",")
                        .append(escapeCsv(ticket.getAffectedVersion() != null ? ticket.getAffectedVersion().getName() : ""))
                        .append("\n");
            }
            System.out.println("CSV scritto con successo: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Esporta tutti i metodi di ogni versione del progetto in un file CSV
     * @param project progetto contenente le versioni e i metodi
     * @param filePath percorso dove salvare il file CSV
     */
    public static void writeMethodsToCsv(Project project, String filePath) {
        ensureParentDirectoryExists(filePath);

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("Version,ClassName,MethodName,FilePath\n");

            List<Version> versions = project.getVersions();
            for (Version version : versions) {
                List<Method> methods = version.getMethods();
                System.out.println("Numero di metodi per la versione " + version.getName() + ": " + (methods != null ? methods.size() : 0));

                if (methods != null) {
                    for (Method method : methods) {
                        writer.append(escapeCsv(version.getName())).append(",")
                                .append(escapeCsv(method.getClassName())).append(",")
                                .append(escapeCsv(method.getSignature())).append(",")
                                .append(escapeCsv(method.getPath())).append("\n");
                    }
                }
            }

            System.out.println("CSV esportato con successo: " + filePath);
        } catch (IOException e) {
            System.err.println("Errore durante l'esportazione dei metodi: " + e.getMessage());
        }
    }

    // 🔒 Ensures that the directory for the file path exists
    private static void ensureParentDirectoryExists(String filePath) {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }
}

