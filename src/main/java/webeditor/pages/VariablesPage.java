// VariablesPage.java
package webeditor.pages;

import org.openqa.selenium.WebDriver;
import io.github.cdimascio.dotenv.Dotenv;

public class VariablesPage extends BasePage {

    // Variables
    public String emailGoogle;
    public String jiraApiKey;
    public String jiraUrl = "https://spothopper.atlassian.net";
    public String jiraFilterPageUrl = jiraUrl + "/issues/";
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    // Constructor
    public VariablesPage() {
        //super(driver);
        //this.driver = driver;
        this.emailGoogle = get("VANJA_EMAIL");
        this.jiraApiKey = get("JIRA_API_KEY");
    }

    // Methods
    public static String get(String key) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            value = dotenv.get(key);
        }
        return value != null ? value.trim() : null;
    }
} // class