package tournament_trail.demo.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import tournament_trail.demo.Utils;
import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.enums.CurrencyCode;
import tournament_trail.demo.entities.enums.TimeControl;
import tournament_trail.demo.entities.enums.TournamentStatus;
import tournament_trail.demo.fixtures.UserFixture;
import tournament_trail.demo.fixtures.dtos.TournamentRequestFixture;
import tournament_trail.demo.repositories.TournamentRepository;
import tournament_trail.demo.repositories.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TournamentCreateTest {

    @LocalServerPort
    private int port;

    private WebDriver webDriver;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @BeforeEach
    public void setUp() {
        webDriver = new ChromeDriver();
    }

    @AfterEach
    public void tearDown() {
        if (webDriver != null) {
            webDriver.quit();
        }
        tournamentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void tournamentCreate_shouldCreateTournament_withValidData() {
        try {
            String rawPassword = "Test123";
            User user = UserFixture.createEnabledUserWithRoleOrganiser(encoder.encode(rawPassword));
            userRepository.save(user);

            webDriver.get("http://localhost:" + port + "/login");

            webDriver.findElement(By.name("username")).sendKeys(user.getUsername());
            webDriver.findElement(By.name("password")).sendKeys(rawPassword);
            webDriver.findElement(By.cssSelector("button[type='submit']")).click();

            WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(5));
            webDriverWait.until(driver -> driver.getCurrentUrl().endsWith("/home"));

            webDriver.get("http://localhost:" + port + "/tournaments/create");

            webDriver.findElement(By.id("name")).sendKeys(TournamentRequestFixture.TEST_NAME);
            webDriver.findElement(By.id("venue")).sendKeys(TournamentRequestFixture.TEST_VENUE);
            webDriver.findElement(By.id("edition")).sendKeys(String.valueOf(TournamentRequestFixture.TEST_EDITION));
            webDriver.findElement(By.id("country")).sendKeys(TournamentRequestFixture.TEST_COUNTRY);
            webDriver.findElement(By.id("city")).sendKeys(TournamentRequestFixture.TEST_CITY);
            webDriver.findElement(By.id("description")).sendKeys(TournamentRequestFixture.TEST_DESCRIPTION);
            webDriver.findElement(By.id("maximumParticipants")).sendKeys("40");
            webDriver.findElement(By.id("entryFee")).sendKeys("40");
            webDriver.findElement(By.id("participationRequirements")).sendKeys(TournamentRequestFixture.TEST_PARTICIPATION_REQUIREMENTS);
            webDriver.findElement(By.id("paymentInstructions")).sendKeys(TournamentRequestFixture.TEST_PAYMENT_INSTRUCTIONS);
            Utils.setDateTimeLocal(webDriver, "registrationDeadline", LocalDateTime.now().plusDays(1));
            Utils.setDateTimeLocal(webDriver, "startTime", LocalDateTime.now().plusDays(2));
            Utils.setDateTimeLocal(webDriver, "endTime", LocalDateTime.now().plusDays(4));
            new Select(webDriver.findElement(By.id("timeControl"))).selectByValue(TimeControl.BLITZ.name());
            new Select(webDriver.findElement(By.id("currency"))).selectByValue(CurrencyCode.EUR.name());

            WebElement ratedCheckbox = webDriver.findElement(By.id("rated"));
            ratedCheckbox.click();

            webDriver.findElement(By.cssSelector(".create-form button[type='submit']")).click();

            webDriverWait.until(driver -> driver.getCurrentUrl().matches(".*/tournaments/[0-9a-fA-F-]{36}$"));

            assertEquals(1, tournamentRepository.count());
            Tournament result = tournamentRepository.findAll().get(0);
            assertEquals(TournamentRequestFixture.TEST_NAME, result.getName());
            assertEquals(TournamentStatus.DRAFT, result.getStatus());
            assertEquals(user.getId(), result.getOrganiser().getId());

        } catch (Throwable e) {
            Utils.takeScreenshot(webDriver, "tournamentCreate");
            throw e;
        }
    }

    @Test
    public void tournamentCreate_shouldShow403_whenUserIsNotOrganiser() {
        try {
            String rawPassword = "Test123";
            User user = UserFixture.createEnabledUser(encoder.encode(rawPassword));
            userRepository.save(user);

            webDriver.get("http://localhost:" + port + "/login");

            webDriver.findElement(By.name("username")).sendKeys(user.getUsername());
            webDriver.findElement(By.name("password")).sendKeys(rawPassword);
            webDriver.findElement(By.cssSelector("button[type='submit']")).click();

            WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(5));

            webDriverWait.until(driver -> driver.getCurrentUrl().endsWith("/home"));

            webDriver.get("http://localhost:" + port + "/tournaments/create");

            WebElement errorCode = webDriverWait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-badge")));

            assertEquals("403", errorCode.getText());

        } catch (Throwable e) {
            Utils.takeScreenshot(webDriver, "tournamentCreate-playerAccess");
            throw e;
        }
    }

    @Test
    public void tournamentCreate_shouldDisplayValidationErrors_whenDataIsInvalid() {
        try {
            String rawPassword = "Test123";
            User user = UserFixture.createEnabledUserWithRoleOrganiser(encoder.encode(rawPassword));
            userRepository.save(user);

            webDriver.get("http://localhost:" + port + "/login");

            webDriver.findElement(By.name("username")).sendKeys(user.getUsername());
            webDriver.findElement(By.name("password")).sendKeys(rawPassword);
            webDriver.findElement(By.cssSelector("button[type='submit']")).click();

            WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(5));
            webDriverWait.until(driver -> driver.getCurrentUrl().endsWith("/home"));

            webDriver.get("http://localhost:" + port + "/tournaments/create");

            webDriver.findElement(By.id("name")).sendKeys("a");
            webDriver.findElement(By.id("venue")).sendKeys(TournamentRequestFixture.TEST_VENUE);
            webDriver.findElement(By.id("edition")).sendKeys("2");
            webDriver.findElement(By.id("country")).sendKeys(TournamentRequestFixture.TEST_COUNTRY);
            webDriver.findElement(By.id("city")).sendKeys(TournamentRequestFixture.TEST_CITY);
            webDriver.findElement(By.id("description")).sendKeys(TournamentRequestFixture.TEST_DESCRIPTION);
            webDriver.findElement(By.id("maximumParticipants")).sendKeys("40");
            webDriver.findElement(By.id("entryFee")).sendKeys("10");
            webDriver.findElement(By.id("participationRequirements")).sendKeys(TournamentRequestFixture.TEST_PARTICIPATION_REQUIREMENTS);
            webDriver.findElement(By.id("paymentInstructions")).sendKeys(TournamentRequestFixture.TEST_PAYMENT_INSTRUCTIONS);
            Utils.setDateTimeLocal(webDriver, "registrationDeadline", LocalDateTime.now().plusDays(1));
            Utils.setDateTimeLocal(webDriver, "startTime", LocalDateTime.now().plusDays(2));
            Utils.setDateTimeLocal(webDriver, "endTime", LocalDateTime.now().plusDays(4));
            new Select(webDriver.findElement(By.id("timeControl"))).selectByValue(TimeControl.BLITZ.name());
            new Select(webDriver.findElement(By.id("currency"))).selectByValue(CurrencyCode.EUR.name());

            webDriver.findElement(By.cssSelector(".create-form button[type='submit']")).click();

            List<WebElement> errors = webDriverWait.until(
                    ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".field-error")));

            WebElement nameError = webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath
                            ("//input[@id='name']/following-sibling::*[contains(@class,'field-error')]")));

            assertTrue(webDriver.getCurrentUrl().endsWith("/tournaments/create"));
            assertFalse(errors.isEmpty(), "Validation error messages should be displayed");
            assertEquals(0, tournamentRepository.count());
            assertEquals("Name must be between 3 and 100 characters", nameError.getText());

        } catch (Throwable e) {
            Utils.takeScreenshot(webDriver, "shouldDisplayValidationErrors");
            throw e;
        }
    }

}
