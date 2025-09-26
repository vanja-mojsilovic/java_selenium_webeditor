// WebEditorPage.java
package webeditor.pages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.openqa.selenium.By;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openqa.selenium.JavascriptExecutor;
import java.io.InputStream;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;


public class WebEditorPage extends BasePage {



    // Methods


    private String buildJqlPayload(String jql) {
        return """
        {
          "jql": "%s",
          "fields": ["key", "customfield_10053", "comment"]
        }
    """.formatted(jql.replace("\"", "\\\""));
    }

    private String buildJqlPayloadForParentKey(String jql) {
        return """
        {
          "jql": "%s",
          "fields": ["key", "customfield_10053", "parent"]
        }
    """.formatted(jql.replace("\"", "\\\""));
    }

    private HttpURLConnection createConnection(String apiUrl, String auth) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Basic " + auth);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        return conn;
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        int status = conn.getResponseCode();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream(),
                StandardCharsets.UTF_8))) {
            String response = reader.lines().collect(Collectors.joining("\n"));
            if (status >= 200 && status < 300) return response;
            throw new IOException("Request failed with status " + status + ": " + response);
        }
    }

    private JSONObject extractSpotSampleLinks(JSONObject json) {
        JSONObject result = new JSONObject();
        JSONArray issues = json.getJSONArray("issues");
        for (int i = 0; i < issues.length(); i++) {
            JSONObject issue = issues.getJSONObject(i);
            String key = issue.getString("key");
            String spotId = issue.getJSONObject("fields").optString("customfield_10053", "null");
            List<String> testSiteUrls = extractSpotSampleUrls(issue);
            String firstUrl = testSiteUrls.isEmpty() ? "null" : testSiteUrls.get(0);
            JSONObject entry = new JSONObject();
            entry.put("issue_key", key);
            entry.put("spot_id", spotId);
            entry.put("test_site_url", firstUrl);
            result.put(key, entry);
        }
        return result;
    }

    private JSONObject extractKeysAndParentKeys(JSONObject json) {
        JSONObject result = new JSONObject();
        JSONArray issues = json.getJSONArray("issues");
        for (int i = 0; i < issues.length(); i++) {
            JSONObject issue = issues.getJSONObject(i);
            String key = issue.getString("key");
            String spotId = issue.getJSONObject("fields").optString("customfield_10053", "null");
            String parentKey = extractParentKeys(issue);
            JSONObject entry = new JSONObject();
            entry.put("issue_key", key);
            entry.put("spot_id", spotId);
            entry.put("parent_key", parentKey);
            result.put(key, entry);
        }
        return result;
    }

    private List<String> extractSpotSampleUrls(JSONObject issue) {
        List<String> urls = new ArrayList<>();
        JSONArray comments = issue.getJSONObject("fields")
                .optJSONObject("comment")
                .optJSONArray("comments");
        if (comments == null) return urls;
        for (int j = 0; j < comments.length(); j++) {
            JSONObject body = comments.getJSONObject(j).optJSONObject("body");
            if (body == null || !body.has("content")) continue;
            JSONArray blocks = body.getJSONArray("content");
            for (int b = 0; b < blocks.length(); b++) {
                JSONArray parts = blocks.getJSONObject(b).optJSONArray("content");
                if (parts == null) continue;
                for (int p = 0; p < parts.length(); p++) {
                    JSONObject part = parts.getJSONObject(p);
                    if ("inlineCard".equals(part.optString("type"))) {
                        JSONObject attrs = part.optJSONObject("attrs");
                        if (attrs != null) {
                            String url = trimAndRemoveForwardSlash(attrs.optString("url", ""));
                            if (url.contains("spot-sample")) urls.add(url);
                        }
                    }
                    if ("text".equals(part.optString("type"))) {
                        JSONArray marks = part.optJSONArray("marks");
                        if (marks != null) {
                            for (int m = 0; m < marks.length(); m++) {
                                JSONObject mark = marks.getJSONObject(m);
                                if ("link".equals(mark.optString("type"))) {
                                    JSONObject attrs = mark.optJSONObject("attrs");
                                    if (attrs != null) {
                                        String href = trimAndRemoveForwardSlash(attrs.optString("href", ""));
                                        if (href.contains("spot-sample")) urls.add(href);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return urls;
    }

    private String extractParentKeys(JSONObject issue) {
        JSONObject fields = issue.optJSONObject("fields");
        if (fields == null) {
            System.out.println("fields == null");
            return "no parent key";
        }
        JSONObject parent = fields.optJSONObject("parent");
        if (parent == null) {
            System.out.println("parent == null");
            return "no parent key";
        }
        return parent.optString("key", "no parent key");
    }


    private void sendPayload(HttpURLConnection conn, String payload) throws IOException {
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }
    }

    public JSONObject fetchSpotSampleLinks(String email, String apiToken, String jql) throws Exception {
        System.out.println("JQL: " + jql);
        String apiUrl = "https://spothopper.atlassian.net/rest/api/3/search/jql";
        String auth = Base64.getEncoder().encodeToString((email + ":" + apiToken).getBytes(StandardCharsets.UTF_8));
        String payload = buildJqlPayload(jql);
        HttpURLConnection conn = createConnection(apiUrl, auth);
        sendPayload(conn, payload);
        String response = readResponse(conn);
        JSONObject json = new JSONObject(response);
        return extractSpotSampleLinks(json);
    }

    public JSONObject fetchKeysAndParentKeys(String email, String apiToken, String jql) throws Exception {
        System.out.println("JQL: " + jql);
        String apiUrl = "https://spothopper.atlassian.net/rest/api/3/search/jql"; // Move to Variables Class
        String auth = Base64.getEncoder().encodeToString((email + ":" + apiToken).getBytes(StandardCharsets.UTF_8));
        String payload = buildJqlPayloadForParentKey(jql);
        HttpURLConnection conn = createConnection(apiUrl, auth);
        sendPayload(conn, payload);
        String response = readResponse(conn);
        JSONObject json = new JSONObject(response);
        return extractKeysAndParentKeys(json);
    }

} // Class