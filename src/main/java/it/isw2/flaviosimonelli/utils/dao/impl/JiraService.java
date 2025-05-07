package it.isw2.flaviosimonelli.utils.dao.impl;

import it.isw2.flaviosimonelli.model.Project;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.isw2.flaviosimonelli.model.Ticket;
import it.isw2.flaviosimonelli.utils.exception.SystemException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JiraService {
    private final String JIRA_URL = "https://issues.apache.org/jira/rest/api/2";
    private final int MAX_RESULTS_PER_PAGE = 100; // Imposta un limite massimo di risultati per pagina

    // Funzione che esegue la richiesta e ottiene il JSON con supporto alla paginazione
    private JSONObject getJsonFromJira(String jql, int startAt, int maxResults) throws IOException, JSONException, URISyntaxException {
        String url = JIRA_URL + "/search/?jql=" + jql + "&startAt=" + startAt + "&maxResults=" + maxResults;
        // Stampa l'URL per debug
        System.out.println("URL: " + url);

        URI uri = new URI(url);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to get a valid response from JIRA. HTTP response code: " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return new JSONObject(response.toString()); // Restituisce il JSON ottenuto dalla risposta
    }

    // Funzione che traduce il risultato JSON in una lista di Ticket
    public List<Ticket> getFixedBugTickets(Project project) throws SystemException {
        String projName = project.getJiraID();
        // Costruisci la query JQL
        String Jql = "project%20%3D%20" + projName + "%20AND%20issuetype%20%3D%20Bug%20AND%20status%20in%20%28Resolved%2C%20Closed%29%20AND%20resolution%20%3D%20Fixed%20ORDER%20BY%20fixVersion%20ASC";

        List<Ticket> tickets = new ArrayList<>();
        int startAt = 0;
        int totalIssues = 0;
        int processedIssues = 0;
        boolean hasMorePages = true;

        // Loop per gestire la paginazione
        while (hasMorePages) {
            // Ottieni il JSON dalla risposta API per la pagina corrente
            JSONObject jsonResponse;
            try {
                jsonResponse = getJsonFromJira(Jql, startAt, MAX_RESULTS_PER_PAGE);
            } catch (JSONException | IOException | URISyntaxException e) {
                SystemException exception = new SystemException(e.getMessage());
                exception.initCause(e);
                throw exception;
            }

            // Ottieni informazioni di paginazione dal JSON
            totalIssues = jsonResponse.getInt("total");
            JSONArray issues = jsonResponse.getJSONArray("issues");
            int currentPageSize = issues.length();

            if (startAt == 0) {
                System.out.println("Numero totale di ticket trovati: " + totalIssues);
            }

            System.out.println("Elaborazione ticket da " + (startAt + 1) + " a " + (startAt + currentPageSize) + " di " + totalIssues);

            // Elabora i ticket della pagina corrente
            for (int i = 0; i < currentPageSize; i++) {
                JSONObject issue = issues.getJSONObject(i);
                Ticket ticket = new Ticket();
                JSONObject fields = issue.getJSONObject("fields");

                // Estrai i dati necessari e imposta i valori nel ticket
                ticket.setId(issue.getString("id"));
                ticket.setKey(issue.getString("key"));

                // Gestione fixVersion - imposta null se ci sono più di una fixVersion
                JSONArray fixVersions = fields.getJSONArray("fixVersions");
                if (fixVersions.length() == 1) {
                    ticket.setFixVersion(fixVersions.getJSONObject(0).getString("name"));
                } else {
                    ticket.setFixVersion(null); // Null se ci sono 0 o più di 1 fixVersions
                }

                // Gestione affectedVersions
                JSONArray affectedVersionsArray = fields.getJSONArray("versions");
                List<String> affectedVersions = new ArrayList<>();
                for (int j = 0; j < affectedVersionsArray.length(); j++) {
                    affectedVersions.add(affectedVersionsArray.getJSONObject(j).getString("name"));
                }
                ticket.setAffectedVersions(affectedVersions);

                tickets.add(ticket);
            }

            processedIssues += currentPageSize;

            // Verifica se ci sono altre pagine da elaborare
            if (processedIssues >= totalIssues || currentPageSize == 0) {
                hasMorePages = false;
            } else {
                startAt += currentPageSize;
            }
        }

        System.out.println("Totale ticket elaborati: " + tickets.size());

        // Stampa i primi 10 ticket elaborati
        System.out.println("Primi 10 ticket:");
        for (int i = 0; i < Math.min(10, tickets.size()); i++) {
            Ticket ticket = tickets.get(i);
            System.out.println("Ticket " + (i + 1) + ":");
            System.out.println("  ID: " + ticket.getId());
            System.out.println("  Key: " + ticket.getKey());
            System.out.println("  Fix Version: " + ticket.getFixVersion());
            System.out.println("  Affected Versions: " + ticket.getAffectedVersions());
        }

        return tickets;
    }

    /**
     * Recupera tutte le versioni di un progetto da JIRA.
     *
     * @param project modello del progetto
     * @return Lista di stringhe contenenti i nomi delle versioni
     * @throws SystemException Se si verificano errori durante la chiamata API
     */
    public List<String> getReleasesProject(Project project) throws SystemException {
        List<String> releases = new ArrayList<>();
        String projName = project.getJiraID();

        try {
            // Costruisci l'endpoint per le versioni del progetto
            String url = JIRA_URL + "/project/" + projName + "/versions";

            // Stampa l'URL per debug
            System.out.println("URL versioni: " + url);

            // Effettua la chiamata HTTP
            URI uri = new URI(url);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Risposta non valida da JIRA. Codice: " + responseCode);
            }

            // Leggi la risposta
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Elabora il JSONArray restituito
            JSONArray jsonVersions = new JSONArray(response.toString());
            for (int i = 0; i < jsonVersions.length(); i++) {
                JSONObject version = jsonVersions.getJSONObject(i);
                // Aggiungi il nome della versione alla lista solo se il campo "released" è true
                if (version.getBoolean("released")) {
                    releases.add(version.getString("name"));
                }
            }

            System.out.println("Trovate " + releases.size() + " release per il progetto " + projName);
            System.out.println("Trovate " + jsonVersions.length() + " versioni per il progetto " + projName);

            // Stampa le prime 10 release
            int limit = Math.min(10, releases.size());
            System.out.println("\nPrime " + limit + " versioni:");
            for (int i = 0; i < limit; i++) {
                System.out.println((i + 1) + ". " + releases.get(i));
            }

        } catch (JSONException | IOException | URISyntaxException e) {
            SystemException exception = new SystemException("Errore durante il recupero delle versioni: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }

        return releases;
    }
}