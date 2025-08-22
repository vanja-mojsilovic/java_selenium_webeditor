package cta.tests;

import cta.pages.*;

public class LoginTest extends BaseTest {


    public void runTest() {
        setUp();

        System.out.println("*******Fetching Jira Issues for Web Editor test********");

        LoginPage loginPage = new LoginPage(driver);
        VariablesPage variablesPage = new VariablesPage(driver);

        navigate(variablesPage.googleLoginPage);
        sleep(1000);

        loginPage.googleLogin(variablesPage.emailGoogle,variablesPage.passwordGoogle,variablesPage.googleSecretKey);
        sleep(4000);

        navigate(variablesPage.jiraUrl);
        sleep(3000);

        loginPage.jiraSignIn(driver,variablesPage.googleSecretKey);
        sleep(8000);

        tearDown();
        System.exit(0);
    }
}
