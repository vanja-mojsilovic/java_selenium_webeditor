package webeditor.tests;

import java.io.IOException;

public class WebEditorTest extends BaseTest{
    public static void main(String[] args) throws InterruptedException, IOException{

        WebEditorTest test = new WebEditorTest();
        test.setUp();
        System.out.println("******* Web Editor Test ********");
        
        LoginTest loginTest = new LoginTest();
        loginTest.driver = test.driver; 
        loginTest.loginGoogleJira();

        TaskTest taskTest = new TaskTest();
        taskTest.driver = test.driver;
        taskTest.searchTasks();

        test.tearDown();
        System.exit(0);
    }
}
