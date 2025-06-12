package it.isw2.flaviosimonelli.utils.dao;

import it.isw2.flaviosimonelli.model.Project.Project;
import it.isw2.flaviosimonelli.model.Version;
import it.isw2.flaviosimonelli.utils.Comparator.NameVersionComparator;
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Service for interacting with the Jira REST API.
 * Provides methods to retrieve project information, versions and tickets.
 */
public class JiraService {
    private static final Logger LOGGER = Logger.getLogger(JiraService.class.getName());
    private static final String JIRA_URL = "https://issues.apache.org/jira/rest/api/2";
    private static final int MAX_RESULTS_PER_PAGE = 100;

    /**
     * Retrieves JSON data from Jira API with pagination support.
     *
     * @param jql JQL query string
     * @param startAt Starting index for pagination
     * @param maxResults Maximum results to return per page
     * @return JSONObject containing API response
     * @throws IOException If connection fails
     * @throws JSONException If parsing fails
     * @throws URISyntaxException If URL is malformed
     */
    private JSONObject getJsonFromJira(String jql, int startAt, int maxResults) throws IOException, JSONException, URISyntaxException {
        String url = JIRA_URL + "/search/?jql=" + jql + "&startAt=" + startAt + "&maxResults=" + maxResults;
        LOGGER.info("URL: " + url);

        URI uri = new URI(url);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to get a valid response from JIRA. HTTP response code: " + responseCode);
        }

        String response = readResponseFromConnection(connection);
        return new JSONObject(response);
    }

    /**
     * Gets a list of fixed bug tickets for a project.
     *
     * @param project The project to query
     * @return List of Ticket objects
     * @throws SystemException If API calls fail
     */
    public List<Ticket> getFixedBugTickets(Project project) throws SystemException {
        String projName = project.getJiraID();
        String jql = buildFixedBugsJql(projName);

        List<Ticket> tickets = new ArrayList<>();
        int startAt = 0;
        int totalIssues = 0;
        int processedIssues = 0;
        boolean hasMorePages = true;

        while (hasMorePages) {
            try {
                JSONObject jsonResponse = getJsonFromJira(jql, startAt, MAX_RESULTS_PER_PAGE);

                // Parse pagination info
                totalIssues = jsonResponse.getInt("total");
                JSONArray issues = jsonResponse.getJSONArray("issues");
                int currentPageSize = issues.length();

                logPaginationInfo(startAt, currentPageSize, totalIssues);

                // Process tickets from current page
                processTicketsFromJson(issues, tickets, project);

                // Update pagination variables
                processedIssues += currentPageSize;
                hasMorePages = processedIssues < totalIssues && currentPageSize > 0;
                startAt += currentPageSize;

            } catch (JSONException | IOException | URISyntaxException e) {
                LOGGER.log(Level.SEVERE, "Error fetching tickets", e);
                throw new SystemException("Error fetching tickets: " + e.getMessage(), e);
            }
        }

        return tickets;
    }

    /**
     * Creates JQL query for fixed bugs.
     *
     * @param projectName The Jira project ID
     * @return URL encoded JQL query string
     */
    private String buildFixedBugsJql(String projectName) {
        return "project%20%3D%20" + projectName +
                "%20AND%20issuetype%20%3D%20Bug%20AND%20status%20in%20" +
                "%28Resolved%2C%20Closed%29%20AND%20resolution%20%3D%20Fixed%20" +
                "ORDER%20BY%20fixVersion%20ASC";
    }

    /**
     * Logs pagination information.
     *
     * @param startAt Starting index
     * @param currentPageSize Current page size
     * @param totalIssues Total issues count
     */
    private void logPaginationInfo(int startAt, int currentPageSize, int totalIssues) {
        if (startAt == 0) {
            LOGGER.info("Total tickets found: " + totalIssues);
        }
        LOGGER.info("Processing tickets from " + (startAt + 1) +
                " to " + (startAt + currentPageSize) + " of " + totalIssues);
    }

    /**
     * Processes JSON issues and converts them to Ticket objects.
     *
     * @param issues JSON array of issues
     * @param tickets List to add tickets to
     * @param project Current project
     */
    private void processTicketsFromJson(JSONArray issues, List<Ticket> tickets, Project project) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        for (int i = 0; i < issues.length(); i++) {
            JSONObject issue = issues.getJSONObject(i);
            JSONObject fields = issue.getJSONObject("fields");

            Ticket ticket = new Ticket();

            // Set ticket ID
            ticket.setId(issue.getString("key"));

            // Set open date
            String createdDate = fields.getString("created");
            ticket.setOpenDate(ZonedDateTime.parse(createdDate, formatter));

            // Handle affectedVersions
            JSONArray affectedVersions = fields.getJSONArray("versions");
            Version affectedVersion = findMatchingVersion(project, affectedVersions, true);
            ticket.setInjectedVersion(affectedVersion);

            tickets.add(ticket);
        }
    }

    /**
     * Finds matching version from JSON versions array.
     *
     * @param project Current project
     * @param versionsArray JSON array containing versions
     * @param findFirst If true, finds first valid version; if false, finds last valid version
     * @return Matching Version object or null if none found
     */
    private Version findMatchingVersion(Project project, JSONArray versionsArray, boolean findFirst) {
        List<String> versionNames = extractVersionNames(versionsArray);

        List<String> sortedVersions = versionNames.stream()
                .sorted(new NameVersionComparator())
                .collect(Collectors.toList());

        if (findFirst) {
            // Find first valid version (for affected versions)
            for (String versionName : sortedVersions) {
                Version version = project.getVersionFromName(versionName);
                if (version != null) {
                    return version;
                }
            }
        } else {
            // Find last valid version (for fix versions)
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
     * Extracts version names from JSON array.
     *
     * @param versionsArray JSON array containing version objects
     * @return List of version name strings
     */
    private List<String> extractVersionNames(JSONArray versionsArray) {
        List<String> versionNames = new ArrayList<>();
        for (int i = 0; i < versionsArray.length(); i++) {
            versionNames.add(versionsArray.getJSONObject(i).getString("name"));
        }
        return versionNames;
    }

    /**
     * Retrieves all versions for a project from Jira.
     *
     * @param project The project model
     * @return List of Version objects
     * @throws SystemException If API calls fail
     */
    public List<Version> getVersions(Project project) throws SystemException {
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
                throw new IOException("Invalid response from JIRA. Code: " + responseCode);
            }

            String response = readResponseFromConnection(connection);
            JSONArray jsonVersions = new JSONArray(response);

            for (int i = 0; i < jsonVersions.length(); i++) {
                JSONObject versionJson = jsonVersions.getJSONObject(i);
                Version version = new Version();
                version.setName(versionJson.getString("name"));
                version.setReleased(versionJson.getBoolean("released"));
                versions.add(version);
            }

            LOGGER.info("Found " + versions.size() + " versions for project " + projName);

        } catch (JSONException | IOException | URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving versions", e);
            throw new SystemException("Error retrieving versions: " + e.getMessage(), e);
        }

        return versions;
    }

    /**
     * Reads response from an HTTP connection.
     *
     * @param connection The HTTP connection
     * @return Response as string
     * @throws IOException If reading fails
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