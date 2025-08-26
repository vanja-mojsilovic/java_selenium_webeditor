// WebEditorTest.java
package webeditor.tests;

import java.io.IOException;

import webeditor.pages.VariablesPage;
import webeditor.pages.WebEditorPage;

public class WebEditorTest extends BaseTest {

    public static void main(String[] args) throws InterruptedException, IOException {
        WebEditorTest test = new WebEditorTest();
        test.setUp();
        // Test API authentication
        VariablesPage vars = new VariablesPage(null); // no driver needed for this
        WebEditorPage webEditorPage = new WebEditorPage(null);
        //webEditorPage.testMyself(vars.emailGoogle, vars.jiraApiKey);
        try {
            System.out.println("******* Web Editor Test ********");

            //  Only perform UI login if NOT running in CI
            if (System.getenv("CI") != null) {
                System.out.println("Running in CI!");
            } 
            System.out.println("Running locally: Performing UI login via Google SSO...");
            LoginTest loginTest = new LoginTest();
            loginTest.driver = test.driver;
            loginTest.loginGoogleJira();
            // Run task logic (uses API for JQL, so works with or without UI login)
            TaskTest taskTest = new TaskTest();
            taskTest.driver = test.driver;
            taskTest.searchTasks_1();

        } catch (Exception e) {
            System.err.println("Test failed with exception:");
            e.printStackTrace();
            throw e;
        } finally {
            // Always clean up
            test.tearDown();
            System.out.println("Browser closed. Test finished.");
        }

        // Optional: exit cleanly
        System.exit(0);
    }
}
