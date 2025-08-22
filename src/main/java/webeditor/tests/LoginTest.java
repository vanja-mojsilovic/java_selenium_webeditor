package webeditor.tests;

import webeditor.pages.*;

public class LoginTest extends BaseTest {


    public void loginGoogleJira() {
        

        LoginPage loginPage = new LoginPage(driver);
        VariablesPage variablesPage = new VariablesPage(driver);

        variablesPage.navigate(variablesPage.googleLoginPage);
        sleep(1000);

        loginPage.googleLogin(variablesPage.emailGoogle,variablesPage.passwordGoogle,variablesPage.googleSecretKey);
        sleep(4000);

        variablesPage.navigate(variablesPage.jiraUrl);
        sleep(3000);

        loginPage.jiraSignIn(driver,variablesPage.googleSecretKey);
        sleep(8000);

        
    }
}
