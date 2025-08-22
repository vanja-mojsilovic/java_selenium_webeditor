package webeditor.pages;
import webeditor.pages.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class WebEditorPage extends BasePage{
    // Variables
    private WebDriver driver;

    // Locators
    @FindBy(xpath = "//input[@id='identifierId']")
    public WebElement emailGoogleLocator; // delete this locator

     // Constructor
    public WebEditorPage(WebDriver driver) {
        super(driver);
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    // Methods
    public String getKeyIssuesByApi(WebDriver driver,String jql,String enteredKeyIssues) throws InterruptedException, IOException {
		String encodedJql = URLEncoder.encode(jql, "UTF-8");
        String apiQueryUrl = "https://spothopper.atlassian.net/rest/api/3/search?jql=" + encodedJql+"&maxResults=1000";
        System.out.println("apiQueryUrl: "+apiQueryUrl);
        navigate(apiQueryUrl);
        Thread.sleep(4000);
		String tasksInJson = getTasksInJson(driver);
		String regex = "\"key\"\\s*:\\s*\"(WEB-\\d+)\"\\s*,\\s*\"fields\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(tasksInJson);
        
		int numb = 0;
	        while (matcher.find()) {
	        	numb++;
	        	String keyIssueFetched = matcher.group(1);
	            if(numb == 1){
	            	enteredKeyIssues = keyIssueFetched;
	            }else {
	            	enteredKeyIssues += "," + keyIssueFetched;
	            }
	        }
	    System.out.println("Number of tasks: "+numb+", KeyIssues: "+enteredKeyIssues);
	    return enteredKeyIssues;
	}

    public String getTasksInJson(WebDriver driver) throws JsonMappingException, JsonProcessingException {
		String result = "";
		WebElement element = waitForVisibilityOfElement(driver, apiPreLocator, 15) ;
		String tasksInJson = element.getText();
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
		result = objectWriter.writeValueAsString(objectMapper.readTree(tasksInJson));
		return result;
	}

    public int getIssueKeyParentIsssueKeyFromApi(WebDriver driver,String apiUrl,List<String> issueKeyCollection,List<String> parentIssueKeyCollection) throws InterruptedException, IOException {
		int numberResult = 0;
        String script =
                "return (async function() {" +
                        "const response = await fetch('" + apiUrl + "', {" +
                        "    method: 'GET'," +
                        "    headers: { 'Accept': 'application/json' }" +
                        "});" +
                        "const data = await response.json();" +
                        "if (data.issues) {" +
                        "    return JSON.stringify(data.issues.map(issue => ({" +
                        "        key: issue.key," +
                        "        parentKey: issue.fields?.parent?.key || 'N/A'" +
                        "    }))); " +
                        "} else {" +
                        "    return JSON.stringify([]);" +
                        "}" +
                        "})()";
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String jsonResult = (String) js.executeScript(script);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode issuesNode = objectMapper.readTree(jsonResult);
        if (issuesNode.isArray()) {
            for (JsonNode issue : issuesNode) {
                String issueKey = issue.get("key").asText();
                String parentKey = issue.get("parentKey").asText();
                issueKeyCollection.add(issueKey);
                parentIssueKeyCollection.add(parentKey);
                System.out.println(numberResult + ". Issue Key: " + issueKey + ", Parent Issue Key: " + parentKey);
                numberResult++;
            }
        } else {
            System.out.println("No issues found.");
        }
        return numberResult;
    }

    public boolean getChildTasksSummaries(WebDriver driver, 
			String apiQueryUrl,
			List<String> childIssuesSummariesList,
			List<String> childIssuesKeyList,
			List<String> childIssuesStatusList
			) {
	    boolean result = false;
	    try {
	        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
	        String script =
	            "var callback = arguments[arguments.length - 1];" +  
	            "(async function(apiUrl) {" +
	            "    try {" +
	            "        const response = await fetch(apiUrl, { " +
	            "            method: 'GET', " +
	            "            headers: { 'Accept': 'application/json', 'Authorization': 'Bearer YOUR_API_TOKEN' }" +
	            "        });" +
	            "        if (!response.ok) throw new Error(`HTTP error! Status: ${response.status}`);" +
	            "        const data = await response.json();" +
	            "        const results = data.issues.map(issue => [" +
	            "            issue.key, issue.fields.status.name, issue.fields.summary" +
	            "        ]);" +
	            "        callback(results);" +
	            "    } catch (error) {" +
	            "        console.error('Error fetching issues:', error);" +
	            "        callback([]);" +
	            "    }" +
	            "})(arguments[0]);";

	        Object jsResult = jsExecutor.executeAsyncScript(script, apiQueryUrl);
	        if (jsResult instanceof List) {
	            List<List<String>> issuesData = (List<List<String>>) jsResult;
	            for (List<String> issue : issuesData) {
	            	String issueKey = issue.get(0);
	            	String status = issue.get(1).toLowerCase();
	            	String summary = issue.get(2).toLowerCase().trim();
	                childIssuesKeyList.add(issueKey);     
	                childIssuesStatusList.add(status);  
	                childIssuesSummariesList.add(summary); 
	                if(summary.contains("website editor")) {
	                	System.out.println(">Epic task has Website Editor child task: " + issueKey + "!");
	                	if(!(status.equals("closed") || status.equals("done"))) {
	                		result = true;
	                		System.out.println(">>>Website Editor task is not closed or done!");
	                	}
	                }
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return result;
	}

    public void enterNewJiraComment(WebDriver driver, String issueKey, String comment) {
	    JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
	    String jsCode =
	    	"var callback = arguments[arguments.length - 1];" +
	        "fetch(`https://spothopper.atlassian.net/rest/api/3/issue/" + issueKey + "/comment`, {" +
	        "    method: 'POST'," +
	        "    headers: {" +
	        "        'Content-Type': 'application/json'," +
	        "        'Accept': 'application/json'" +
	        "    }," +
	        "    body: JSON.stringify({" +
	        "        body: {" +
	        "            type: 'doc'," +
	        "            version: 1," +
	        "            content: [" +
	        "                {" +
	        "                    type: 'paragraph'," +
	        "                    content: [" +
	        "                        {" +
	        "                            type: 'text'," +
	        "                            text: '" + comment + "'" +
	        "                        }" +
	        "                    ]" +
	        "                }" +
	        "            ]" +
	        "        }" +
	        "    })" +
	        "})" +
	        ".then(response => {" +
	        "    if (response.ok) {" +
	        "        response.json().then(data => callback(data));" +
	        "    } else {" +
	        "        callback(`Failed to add comment: ${response.status} ${response.statusText}`);" +
	        "    }" +
	        "})" +
	        ".then(data => {" +
	        "    console.log('Comment created successfully:', data);" +
	        "})" +
	        ".catch(error => {" +
	        "    callback(`Error: ${error.message}`);" +
	        "});";
	    jsExecutor.executeScript(jsCode);
	    System.out.println("Jira comment entered.");
	}


}
