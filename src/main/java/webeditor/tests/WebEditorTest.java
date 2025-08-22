package webeditor.tests;


public class WebEditorTest extends BaseTest{
    public static void main(String[] args) {

        WebEditorTest test = new WebEditorTest();
        test.setUp();
        System.out.println("*******Fetching Jira Issues for Web Editor test********");
        
        LoginTest loginTest = new LoginTest();
        loginTest.driver = test.driver; 
        loginTest.loginGoogleJira();

        test.tearDown();
        System.exit(0);
    }
}
