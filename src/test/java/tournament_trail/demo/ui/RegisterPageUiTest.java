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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tournament_trail.demo.Utils;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.enums.Role;
import tournament_trail.demo.fixtures.UserFixture;
import tournament_trail.demo.repositories.UserRepository;
import tournament_trail.demo.services.EmailService;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class RegisterPageUiTest {

    @LocalServerPort
    private int port;

    private WebDriver webDriver;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private EmailService emailService;


    @BeforeEach
    public void setUp() {
        webDriver = new ChromeDriver();
        webDriver.manage().window().setSize(new Dimension(1440, 1000));
    }

    @AfterEach
    public void tearDown() {
        if (webDriver != null) {
            webDriver.quit();
        }
    }

    @Test
    public void registerPage_shouldRegisterUser() {
        try {
            webDriver.get("http://localhost:" + port + "/register");

            webDriver.findElement(By.name("username")).sendKeys(UserFixture.TEST_USERNAME);
            webDriver.findElement(By.name("email")).sendKeys("example@email.com");
            webDriver.findElement(By.name("password")).sendKeys("TestPassword123");

            WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(5));

            WebElement registerButton = webDriverWait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button.button.button-primary.submit-button")));

            ((JavascriptExecutor) webDriver).executeScript(
                    "arguments[0].scrollIntoView({block: 'center'});", registerButton);

            registerButton.click();

            webDriverWait.until(driver -> driver.getCurrentUrl().endsWith("/login"));

            assertTrue(webDriver.getCurrentUrl().endsWith("/login"),
                    "User should be redirected to /login after registration");

            User user = userRepository.findByUsername(UserFixture.TEST_USERNAME).orElseThrow();

            assertFalse(user.isEnabled(), "New user should be disabled until email verification");
            assertEquals(Role.PLAYER, user.getRole());
            verify(emailService).sendVerificationEmail(eq("example@email.com"), any(String.class));

        } catch (Throwable exception) {
            Utils.takeScreenshot(webDriver, "register-failed");
            throw exception;
        }
    }

    @Test
    public void registerPage_withInvalidData_shouldDisplayError() {
        try {
            webDriver.get("http://localhost:" + port + "/register");
            webDriver.findElement(By.name("username")).sendKeys("a");
            webDriver.findElement(By.name("email")).sendKeys("a@example.com");
            webDriver.findElement(By.name("password")).sendKeys("abc");

            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(5));

            WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
                                    "button.button.button-primary.submit-button")));

            ((JavascriptExecutor) webDriver).executeScript(
                    "arguments[0].scrollIntoView({block: 'center'});", registerButton);

            registerButton.click();

            List<WebElement> errorMessages = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                            By.cssSelector(".field-error")));

            assertTrue(webDriver.getCurrentUrl().endsWith("/register"),
                    "User should remain on registration page after invalid input");

            assertFalse(errorMessages.isEmpty(), "Validation error messages should be displayed");
            assertTrue(userRepository.findByUsername("a").isEmpty()
                    , "Invalid user should not be saved");

        } catch (Throwable exception) {
            Utils.takeScreenshot(webDriver, "invalid-registration-failed");

            throw exception;
        }
    }
}
