package it.isw2.flaviosimonelli.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
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
            writer.append("id,name,archived,released,releaseDate\n");

            for (Version version : versions) {
                writer.append(escapeCsv(version.getId())).append(",")
                        .append(escapeCsv(version.getName())).append(",")
                        .append(Boolean.toString(version.isArchived())).append(",")
                        .append(Boolean.toString(version.isReleased())).append(",")
                        .append(version.getReleaseDate() != null ? escapeCsv(version.getReleaseDate().toString()) : "")
                        .append("\n");
            }
            System.out.println("CSV scritto con successo: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void writeTicketsToCsv(List<Ticket> tickets, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("id,key,url,fixVersion,affectedVersion,priorityName,assignee,creator,reporter,summary,description,created,updated,resolutionDate,votes,watchers\n");
            for (Ticket ticket : tickets) {
                writer.append(escapeCsv(ticket.getId())).append(",")
                        .append(escapeCsv(ticket.getKey())).append(",")
                        .append(escapeCsv(ticket.getUrl())).append(",")
                        .append(escapeCsv(String.join(";", ticket.getFixVersion()))).append(",")
                        .append(escapeCsv(String.join(";", ticket.getAffectedVersion()))).append(",")
                        .append(escapeCsv(ticket.getPriorityName())).append(",")
                        .append(escapeCsv(ticket.getAssignee())).append(",")
                        .append(escapeCsv(ticket.getCreator())).append(",")
                        .append(escapeCsv(ticket.getReporter())).append(",")
                        .append(escapeCsv(ticket.getSummary())).append(",")
                        .append(escapeCsv(ticket.getDescription())).append(",")
                        .append(ticket.getCreated() != null ? escapeCsv(ticket.getCreated().toString()) : "").append(",")
                        .append(ticket.getUpdated() != null ? escapeCsv(ticket.getUpdated().toString()) : "").append(",")
                        .append(ticket.getResolutionDate() != null ? escapeCsv(ticket.getResolutionDate().toString()) : "").append(",")
                        .append(Integer.toString(ticket.getVotes())).append(",")
                        .append(Integer.toString(ticket.getWatchers()))
                        .append("\n");
            }
            System.out.println("CSV scritto con successo: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
