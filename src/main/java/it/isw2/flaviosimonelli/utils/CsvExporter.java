package it.isw2.flaviosimonelli.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import it.isw2.flaviosimonelli.model.Method;
import it.isw2.flaviosimonelli.model.Project.Project;
import it.isw2.flaviosimonelli.model.Version;
import it.isw2.flaviosimonelli.model.Ticket;


public class CsvExporter {

    private static String escapeCsv(String field) {
        if (field == null) return "";
        String escaped = field.replace("\"", "\"\""); // Escapa i doppi apici
        return "\"" + escaped + "\""; // Racchiude tutto tra virgolette
    }

    public static void writeVersionsToCsv(List<Version> versions, String filePath) {
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
    public static void writeMethodsToCsv(Project project, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Intestazione del CSV
            writer.append("Version,ClassName,MethodName,FilePath\n");

            List<Version> versions = project.getVersions();
            for (Version version : versions) {
                List<Method> methods = version.getMethods();
                if (methods != null) {
                    for (Method method : methods) {
                        writer.append(version.getName())
                                .append(",")
                                .append(method.getClassName())
                                .append(",")
                                .append(method.getName().replace(",", ";")) // Evita problemi con virgole nel nome del metodo
                                .append(",")
                                .append(method.getPath())
                                .append("\n");
                    }
                }
            }

            System.out.println("CSV esportato con successo: " + filePath);
        } catch (IOException e) {
            System.err.println("Errore durante l'esportazione dei metodi: " + e.getMessage());
        }
    }

}
