import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

    public class JiraService {

        private static final String BASE_URL = "https://issues.apache.org/jira/rest/api/2/search";

        public List<JiraTicket> getFixedBugs(String projectKey) throws IOException, InterruptedException {
            List<JiraTicket> results = new ArrayList<>();
            String jql = String.format("project=%s AND issuetype=Bug AND resolution=Fixed", projectKey);
            String fullUrl = BASE_URL + "?jql=" + URLEncoder.encode(jql, "UTF-8");

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Errore HTTP: " + response.statusCode());
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            for (JsonNode issue : root.get("issues")) {
                String key = issue.get("key").asText();
                JsonNode fields = issue.get("fields");
                String summary = fields.get("summary").asText();
                String status = fields.get("status").get("name").asText();
                String resolution = fields.get("resolution").get("name").asText();

                results.add(new JiraTicket(key, summary, status, resolution));
            }

            return results;
        }
    }
