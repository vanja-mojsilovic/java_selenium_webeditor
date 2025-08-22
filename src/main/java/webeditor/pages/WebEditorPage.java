package webeditor.pages;
import webeditor.pages.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.regex.Matcher;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

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
    
}
