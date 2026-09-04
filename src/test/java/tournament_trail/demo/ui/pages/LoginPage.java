package tournament_trail.demo.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
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

        webDriver.findElement(By.name("username")).sendKeys(username);
        webDriver.findElement(By.name("password")).sendKeys(password);
        webDriver.findElement(By.cssSelector("button[type='submit']")).click();

        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(5));

        wait.until(driver -> driver.getCurrentUrl().endsWith("/home"));
    }
}