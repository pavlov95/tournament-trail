package tournament_trail.demo.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {

    private final WebDriver webDriver;
    private final String baseUrl;

    public LoginPage(WebDriver webDriver, int port) {
        this.webDriver = webDriver;
        this.baseUrl = "http://localhost:" + port;
    }

    public void login(String username, String password) {
        webDriver.get(baseUrl + "/login");
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys(username);

        webDriver.findElement(By.name("password")).sendKeys(password);

        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("form.auth-form button[type='submit']")));

        ((JavascriptExecutor) webDriver).executeScript(
                "arguments[0].scrollIntoView({block: 'center'});", loginButton);

        loginButton.click();

        wait.until(driver -> driver.getCurrentUrl().endsWith("/home"));
    }
}