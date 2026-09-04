package tournament_trail.demo.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import tournament_trail.demo.Utils;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.fixtures.UserFixture;
import tournament_trail.demo.repositories.UserRepository;
import java.time.Duration;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LoginPageUiTest {

    @LocalServerPort
    private int port;

    private WebDriver driver;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;

    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }

        if (user != null && user.getId() != null) {
            userRepository.deleteById(user.getId());
        }
    }

    @Test
    public void loginPage_shouldDisplayLoginForm() {
        driver.get("http://localhost:" + port + "/login");

        WebElement usernameInput = driver.findElement(By.name("username"));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        assertTrue(usernameInput.isDisplayed(), "Username input should be visible");
        assertTrue(passwordInput.isDisplayed(), "Password input should be visible");
        assertTrue(loginButton.isDisplayed(), "Login button should be visible");
    }

    @Test
    void loginPage_withValidCredentials_shouldRedirectToHome() {
        user = UserFixture.createEnabledUser();
        user.setPassword(passwordEncoder.encode("Password123"));
        userRepository.save(user);

        try {
            driver.get("http://localhost:" + port + "/login");

            driver.findElement(By.name("username")).sendKeys(user.getUsername());
            driver.findElement(By.name("password")).sendKeys("Password123");
            driver.findElement(By.cssSelector("button[type='submit']")).click();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

            wait.until(webDriver -> Objects.requireNonNull(webDriver.getCurrentUrl()).endsWith("/home"));

            assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).endsWith("/home"),
                    "User should be redirected to /home after successful login");

        } catch (Throwable exception) {

            Utils.takeScreenshot(driver,"successful-login-failed");

            throw exception;
        }
    }

    @Test
    public void loginPage_shouldShowInvalidUserOrPassword() {
        driver.get("http://localhost:" + port + "/login");

        driver.findElement(By.name("username")).sendKeys("WrongUsername");
        driver.findElement(By.name("password")).sendKeys("WrongPassword");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".message-error")));

        assertTrue(driver.getCurrentUrl().contains("/login?error"),
                "User should be redirected to /login?error after invalid credentials");

        assertEquals("Invalid username or password.", errorMessage.getText(),
                "Incorrect error message should not be displayed");
    }

    @Test
    public void loginPage_shouldRedirectToRegisterPage_whenRegisterButtonIsClicked() {
        driver.get("http://localhost:" + port + "/login");

        driver.findElement(By.cssSelector(".auth-switch a")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        wait.until(webDriver -> webDriver.getCurrentUrl().endsWith("/register"));

        assertTrue(driver.getCurrentUrl().endsWith("/register")
                , "Register link should redirect to /register");
    }

    @Test
    public void loginPage_browseTournamentsLink_shouldOpenTournaments() {
        driver.get("http://localhost:" + port + "/login");

        driver.findElement(By.cssSelector(".back-link")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        wait.until(webDriver -> webDriver.getCurrentUrl().endsWith("/tournaments"));

        assertTrue(driver.getCurrentUrl().endsWith("/tournaments"),
                "Browse tournaments link should redirect to /tournaments");
    }


}