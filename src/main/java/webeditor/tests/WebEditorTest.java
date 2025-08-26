// WebEditorTest.java
package webeditor.tests;

import java.io.IOException;

import webeditor.pages.VariablesPage;
import webeditor.pages.WebEditorPage;

public class WebEditorTest extends BaseTest {

    public static void main(String[] args) throws IOException {
        WebEditorTest test = new WebEditorTest();

        // ✅ Set CI flag (GitHub Actions sets this, but good to know)
        boolean isCi = System.getenv("CI") != null;

        if (isCi) {
            // WebEditorTest.java (inside the if (isCi) block)

            System.out.println("🚀 Running in CI: Skipping browser. Using Jira API directly...");

            // 🔐 Load credentials
            String email = System.getenv("VANJA_EMAIL");
            String apiToken = System.getenv("JIRA_API_KEY"); // <-- Corrected name

            // ✅ Add Debugging: Print the values (be careful with the token in logs!)
            System.out.println("📧 Debug - Email Env Var (VANJA_EMAIL): '" + System.getenv("VANJA_EMAIL") + "'");
            System.out.println("🔐 Debug - API Key Env Var (JIRA_API_KEY) Length: " + (System.getenv("JIRA_API_KEY") != null ? System.getenv("JIRA_API_KEY").length() : "null"));
            System.out.println("📧 Debug - Loaded Email: '" + email + "'");
            System.out.println("🔐 Debug - Loaded API Token Length: " + (apiToken != null ? apiToken.length() : "null"));

            // ✅ Check for null or empty values
            if (email == null || email.trim().isEmpty()) {
                System.err.println("❌ ERROR: VANJA_EMAIL environment variable is not set or is empty!");
                System.exit(1); // Stop execution
            }
            if (apiToken == null || apiToken.trim().isEmpty()) {
                System.err.println("❌ ERROR: JIRA_API_KEY environment variable is not set or is empty!");
                System.exit(1); // Stop execution
            }

            // 🧪 Test auth first
            WebEditorPage webEditorPage = new WebEditorPage(null);
            try {
                webEditorPage.testMyself(email, apiToken); // This should now work if credentials are correct
            } catch (IOException e) {
                System.err.println("❌ Authentication test failed. Check credentials above.");
                e.printStackTrace(); // Print the full stack trace for more details
                System.exit(1); // Stop execution if auth fails
            }

            // ✅ If we get here, auth was successful. Proceed with the task.
            System.out.println("✅ Authentication successful. Proceeding with task execution...");
            TaskTest taskTest = new TaskTest();
            taskTest.searchTasks_1(); // This must use getKeyIssuesByApiPost(...), NOT UI

        } else {
            System.out.println("🖥️ Running locally: Using UI login...");
            test.setUp();
            try {
                LoginTest loginTest = new LoginTest();
                loginTest.driver = test.driver;
                loginTest.loginGoogleJira();

                TaskTest taskTest = new TaskTest();
                taskTest.driver = test.driver;
                taskTest.searchTasks_1(); // Can use UI or API
            } finally {
                test.tearDown();
            }
        }

        System.exit(0);
    }
}
