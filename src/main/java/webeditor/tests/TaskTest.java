package webeditor.tests;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import webeditor.pages.*;
import java.util.List;


import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;




public class TaskTest extends BaseTest{
    


    // Variables
    int numberOfTasks;
    private List<String> issueKeyCollection = new ArrayList<>();
    private List<String> parentIssueKeyCollection = new ArrayList<>();
    String logFileMessage = "";
    int counter = 0;

    // Methods
    public void searchTasks_1() throws InterruptedException, IOException {
        String jqlBuildsFilter = "project = WEB AND summary ~ 'Go Live' AND status = QA";
        System.out.println("jqlBuildsFilter: " + jqlBuildsFilter);

        WebEditorPage webEditorPage = new WebEditorPage(driver);
        VariablesPage variablesPage = new VariablesPage(driver);
        webEditorPage.navigate(variablesPage.jiraFilterPageUrl);
        sleep(5000);
        webEditorPage.enterJql(jqlBuildsFilter);
        sleep(1000);
        webEditorPage.clickSearchJql();
        sleep(7000);
        String allKeyIssues = webEditorPage.getAllKeyIssues(driver);
        //String allKeyIssues = webEditorPage.getKeyIssuesByApiPost(jqlBuildsFilter, variablesPage.emailGoogle, variablesPage.jiraApiKey);
        allKeyIssues = "issue in (" + allKeyIssues + ")";
        System.out.println("allKeyIssues: " + allKeyIssues);
        String wasInQaTasks = "summary ~ \"Go Live\" AND status = QA AND comment ~ \"\\\"Please check if there is a Web editor task on this branch!\\\"\"";
        
        webEditorPage.navigate(variablesPage.jiraFilterPageUrl);
        sleep(5000);
        webEditorPage.enterJql(wasInQaTasks);
        sleep(1000);
        webEditorPage.clickSearchJql();
        sleep(7000);
        String wasInQaIssuesSeparatedWithCommas = webEditorPage.getAllKeyIssues(driver);
        String excludedTask = "";
        if (!wasInQaIssuesSeparatedWithCommas.trim().equals("")) {
            excludedTask += " AND issue not in (" + wasInQaIssuesSeparatedWithCommas + ")";
        }

        String resultTasks = allKeyIssues + excludedTask;
        System.out.println("resultTasks: " + resultTasks);

        String encodedJql = URLEncoder.encode(resultTasks, "UTF-8");
        String apiQueryUrl = "https://spothopper.atlassian.net/rest/api/3/search?jql=" + encodedJql + "&maxResults=1000";

        numberOfTasks = webEditorPage.getIssueKeyParentIsssueKeyFromApi(driver, apiQueryUrl, issueKeyCollection, parentIssueKeyCollection);
        System.out.println("numberOfTasks: " + numberOfTasks);

        if (numberOfTasks == 0) {
            System.out.println("numberOfTasks == 0");
            return;
        }

        for (int i = 0; i < numberOfTasks; i++) {
            String issueKey = issueKeyCollection.get(i);
            String parentIssueKey = parentIssueKeyCollection.get(i);
            encodedJql = "parent%20%3D%20" + URLEncoder.encode(parentIssueKey, StandardCharsets.UTF_8);
            apiQueryUrl = "https://spothopper.atlassian.net/rest/api/3/search?jql=" + encodedJql + "&maxResults=1000";
            System.out.println("apiQueryUrl: " + apiQueryUrl);
            webEditorPage.navigate(apiQueryUrl);
            Thread.sleep(1000);

            List<String> childIssuesSummariesList = new ArrayList<>();
            List<String> childIssuesKeyList = new ArrayList<>();
            List<String> childIssuesStatusList = new ArrayList<>();
            boolean hasWebsiteEditorInProgress = webEditorPage.getChildTasksSummaries(driver, apiQueryUrl, childIssuesSummariesList, childIssuesKeyList, childIssuesStatusList);

            for (int j = 0; j < childIssuesSummariesList.size(); j++) {
                System.out.println((j + 1) + ". " + childIssuesSummariesList.get(j) + " : " +
                    childIssuesKeyList.get(j) + " : " +
                    childIssuesStatusList.get(j));
            }

            logFileMessage = (counter + 1) + ". " + issueKey;
            if (hasWebsiteEditorInProgress) {
                String logMessage = " >>>>> Issue found! <<<<< " + issueKey;
                logFileMessage += logMessage;
                System.out.println(logMessage);

                String commentMessage = "Please check if there is a Web editor task on this branch!";
                // <<<<<<<<<<<<<<<<<<<<<<<<< comment out when fix
                //webEditorPage.postJiraComment(issueKey, commentMessage, variablesPage.emailGoogle, variablesPage.jiraApiKey);
            }

            counter++;
            System.out.println("logFileMessage: " + logFileMessage);
            System.out.println(" -- end of the task --");
        }
    }


    

    

   
    
}
