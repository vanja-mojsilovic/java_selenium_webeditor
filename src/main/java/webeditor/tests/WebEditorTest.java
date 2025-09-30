// WebEditorTest.java
package webeditor.tests;

import java.io.IOException;

import org.json.JSONException;
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
        String initialJql = "project = WEB AND summary ~ \"Go Live\" AND status = QA"; // Move to Variables Class
        try {
            JSONObject initialTasks = webEditorPage.fetchKeysAndParentKeys(email, apiToken,initialJql);
            String jqlSuppressed = "comment ~ \"Please check if there is a Web editor task on this branch\" ";
            JSONObject suppressedTasks = webEditorPage.fetchKeysAndParentKeys(email, apiToken,jqlSuppressed);
            JSONObject tasks = new JSONObject();
            for (String taskKey : initialTasks.keySet()) {
                if (!suppressedTasks.has(taskKey)) {
                    tasks.put(taskKey, initialTasks.getJSONObject(taskKey));
                }
            }
            for (String taskKey : tasks.keySet()) {
                JSONObject initialTask = tasks.getJSONObject(taskKey);
                String key = initialTask.getString("issue_key");
                String spotId = initialTask.getString("spot_id");
                String parentKey = initialTask.getString("parent_key");
                System.out.println("key: " + key + ", spotId: " + spotId);
                String parentJql = "parent in (" + parentKey + ")";
                JSONObject childTasksJson = webEditorPage.fetchChildKeys(email,apiToken,parentJql);
                System.out.println("Child tasks:" );
                boolean issueFound = false;
                for(String childTaskKey : childTasksJson.keySet()){
                    JSONObject childTaskJson = childTasksJson.getJSONObject(childTaskKey);
                    String childKey = childTaskJson.getString("issue_key");
                    String summary = childTaskJson.getString("summary");
                    String status = childTaskJson.getString("status");
                    System.out.println("childKey: "+childKey+" summary "+summary+" status: "+status);
                    boolean specificTask = webEditorPage.webEditorTaskNotClosedOrDone(summary,status);
                    issueFound = issueFound || specificTask;
                }
                if(issueFound){
                    String commentText = "Please check if there is a Web editor task on this branch!";
                    System.out.println("Issue found: " + issueFound);
                    webEditorPage.addCommentToIssue(email,apiToken,key,commentText);
                }
                else{
                    String commentText = ", No opened Website Editor tasks!";
                    System.out.println("Issue found: " + issueFound + commentText);
                }


            } // for loop
        } catch (Exception e) {
            System.err.println("Failed to fetch spot sample links: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }


        System.exit(0);
    }
}
