package it.isw2.flaviosimonelli.utils.dao;

import it.isw2.flaviosimonelli.model.Project.Project;
import it.isw2.flaviosimonelli.model.Version;
import it.isw2.flaviosimonelli.model.Ticket;
import it.isw2.flaviosimonelli.utils.Comparator.NameVersionComparator;
import it.isw2.flaviosimonelli.utils.exception.JiraException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * DAO per l'interazione con la Jira REST API.
 * Tutti i metodi pubblici lanciano JiraException in caso di errore.
 */
public class JiraService {
    private static final Logger LOGGER = Logger.getLogger(JiraService.class.getName());
    private static final String JIRA_URL = "https://issues.apache.org/jira/rest/api/2";
    private static final int MAX_RESULTS_PER_PAGE = 100;

    /**
     * Recupera i ticket bug risolti per un progetto.
     *
     * @param project Il progetto da interrogare
     * @return Lista di Ticket
     * @throws JiraException In caso di errore nella chiamata API
     */
    public List<Ticket> getFixedBugTickets(Project project) throws JiraException {

        String projName = project.getJiraID();
        String jql = buildFixedBugsJql(projName);

        List<Ticket> tickets = new ArrayList<>();
        int startAt = 0;
        int totalIssues = 0;
        int processedIssues = 0;
        boolean hasMorePages = true;

        while (hasMorePages) {
            String url = JIRA_URL + "/search/?jql=" + jql + "&startAt=" + startAt + "&maxResults=" + MAX_RESULTS_PER_PAGE;
            try {
                JSONObject jsonResponse = getJsonFromJira(jql, startAt, MAX_RESULTS_PER_PAGE);

                totalIssues = jsonResponse.getInt("total");
                JSONArray issues = jsonResponse.getJSONArray("issues");
                int currentPageSize = issues.length();

                processTicketsFromJson(issues, tickets, project);

                processedIssues += currentPageSize;
                hasMorePages = processedIssues < totalIssues && currentPageSize > 0;
                startAt += currentPageSize;

            } catch (JSONException | IOException | URISyntaxException e) {
                throw new JiraException("Errore durante il recupero dei ticket da Jira: " + e.getMessage(), e.getClass().getSimpleName(), url);
            }
        }

        return tickets;
    }

    /**
     * Recupera tutte le versioni di un progetto da Jira.
     *
     * @param project Il progetto
     * @return Lista di Version
     * @throws JiraException In caso di errore nella chiamata API
     */
    public List<Version> getVersions(Project project) throws JiraException {
        if (project == null) {
            throw new JiraException("Project non può essere null", "IllegalArgumentException", null);
        }

        List<Version> versions = new ArrayList<>();
        String projName = project.getJiraID();
        String url = JIRA_URL + "/project/" + projName + "/versions";

        try {
            LOGGER.info("URL request http: " + url);

            URI uri = new URI(url);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Risposta non valida da JIRA. Codice: " + responseCode);
            }

            String response = readResponseFromConnection(connection);
            JSONArray jsonVersions = new JSONArray(response);

            for (int i = 0; i < jsonVersions.length(); i++) {
                JSONObject versionJson = jsonVersions.getJSONObject(i);
                Version version = new Version();
                version.setName(versionJson.getString("name"));
                version.setReleased(versionJson.optBoolean("released", false));
                versions.add(version);
            }

            LOGGER.info("Trovate " + versions.size() + " versioni per il progetto " + projName);
            return versions;
        } catch (JSONException | IOException | URISyntaxException e) {
            throw new JiraException("Errore durante il recupero delle versioni da Jira: " + e.getMessage(), e.getClass().getSimpleName(), url);
        }
    }

    // --- METODI PRIVATI DI SUPPORTO ---

    /**
     * Costruisce la JQL per i bug risolti.
     */
    private String buildFixedBugsJql(String projectName) {
        return "project%20%3D%20" + projectName +
                "%20AND%20issuetype%20%3D%20Bug%20AND%20status%20in%20" +
                "%28Resolved%2C%20Closed%29%20AND%20resolution%20%3D%20Fixed%20" +
                "ORDER%20BY%20fixVersion%20ASC";
    }

    /**
     * Recupera dati JSON da Jira con supporto paginazione.
     */
    private JSONObject getJsonFromJira(String jql, int startAt, int maxResults) throws IOException, JSONException, URISyntaxException {
        String url = JIRA_URL + "/search/?jql=" + jql + "&startAt=" + startAt + "&maxResults=" + maxResults;
        URI uri = new URI(url);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Risposta non valida da JIRA. Codice: " + responseCode);
        }

        String response = readResponseFromConnection(connection);
        return new JSONObject(response);
    }

    /**
     * Converte issues JSON in Ticket.
     */
    private void processTicketsFromJson(JSONArray issues, List<Ticket> tickets, Project project) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        for (int i = 0; i < issues.length(); i++) {
            JSONObject issue = issues.getJSONObject(i);
            JSONObject fields = issue.getJSONObject("fields");

            Ticket ticket = new Ticket();
            ticket.setId(issue.getString("key"));

            String createdDate = fields.getString("created");
            ticket.setOpenDate(ZonedDateTime.parse(createdDate, formatter));

            JSONArray affectedVersions = fields.getJSONArray("versions");
            Version affectedVersion = findMatchingVersion(project, affectedVersions, true);
            ticket.setInjectedVersion(affectedVersion);

            tickets.add(ticket);
        }
    }

    /**
     * Trova la versione corrispondente da un array JSON.
     */
    private Version findMatchingVersion(Project project, JSONArray versionsArray, boolean findFirst) {
        List<String> versionNames = extractVersionNames(versionsArray);

        List<String> sortedVersions = versionNames.stream()
                .sorted(new NameVersionComparator())
                .collect(Collectors.toList());

        if (findFirst) {
            for (String versionName : sortedVersions) {
                Version version = project.getVersionFromName(versionName);
                if (version != null) {
                    return version;
                }
            }
        } else {
            for (int i = sortedVersions.size() - 1; i >= 0; i--) {
                Version version = project.getVersionFromName(sortedVersions.get(i));
                if (version != null) {
                    return version;
                }
            }
        }
        return null;
    }

    /**
     * Estrae i nomi delle versioni da un array JSON.
     */
    private List<String> extractVersionNames(JSONArray versionsArray) {
        List<String> versionNames = new ArrayList<>();
        for (int i = 0; i < versionsArray.length(); i++) {
            versionNames.add(versionsArray.getJSONObject(i).getString("name"));
        }
        return versionNames;
    }

    /**
     * Legge la risposta da una connessione HTTP.
     */
    private String readResponseFromConnection(HttpURLConnection connection) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}