package webeditor.pages;
import webeditor.pages.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
        Thread.sleep(2000);
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
	            //System.out.println(numb+". "+matcher.group());
	        }
	    System.out.println("Number of tasks: "+numb+", KeyIssues: "+enteredKeyIssues);
	    return enteredKeyIssues;
	}

    public String getTasksInJson(WebDriver driver) throws JsonMappingException, JsonProcessingException {
		String result = "";
		WebElement element = waitForVisibilityOfElement(driver, apiPreLocator, 3) ;
		String tasksInJson = element.getText();
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
		result = objectWriter.writeValueAsString(objectMapper.readTree(tasksInJson));
		return result;
	}
}
