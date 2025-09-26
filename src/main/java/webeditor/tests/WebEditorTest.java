// WebEditorTest.java
package webeditor.tests;

import java.io.IOException;
import webeditor.pages.WebEditorPage;
import webeditor.pages.VariablesPage;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.ArrayList;
import org.openqa.selenium.JavascriptExecutor;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Base64;
import java.util.stream.Collectors;

public class WebEditorTest extends BaseTest {

    public static void main(String[] args) throws IOException {
        WebEditorPage webEditorPage = new WebEditorPage();
        VariablesPage variablesPage = new VariablesPage();
        boolean isCi = System.getenv("CI") != null;
        String email = VariablesPage.get("VANJA_EMAIL");
        String apiToken = VariablesPage.get("JIRA_API_KEY");
        if (email == null || email.trim().isEmpty()) {
            System.err.println("ERROR: VANJA_EMAIL environment variable is not set or is empty!");
            System.exit(1);
        }
        if (apiToken == null || apiToken.trim().isEmpty()) {
            System.err.println(" ERROR: JIRA_API_KEY environment variable is not set or is empty!");
            System.exit(1);
        }
        String jql = "project = WEB AND summary ~ \"Go Live\" AND status = QA"; // Move to Variables Class
        try {
            JSONObject tasks = webEditorPage.fetchKeysAndParentKeys(email, apiToken,jql);
            for (String taskKey : tasks.keySet()) {
                JSONObject task = tasks.getJSONObject(taskKey);
                String key = task.getString("issue_key");
                String spotId = task.getString("spot_id");
                String parentKey = task.getString("parent_key");
                System.out.println(key + " " + spotId + " " + parentKey);
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch spot sample links: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
