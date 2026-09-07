package tournament_trail.demo.ui;

import org.h2.table.DualTable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import tournament_trail.demo.Utils;
import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.TravelGroup;
import tournament_trail.demo.entities.TravelGroupComment;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.fixtures.TournamentFixture;
import tournament_trail.demo.fixtures.TravelGroupFixture;
import tournament_trail.demo.fixtures.UserFixture;
import tournament_trail.demo.repositories.TournamentRepository;
import tournament_trail.demo.repositories.TravelGroupCommentRepository;
import tournament_trail.demo.repositories.TravelGroupRepository;
import tournament_trail.demo.repositories.UserRepository;
import tournament_trail.demo.ui.pages.LoginPage;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CommentsPageTest {
    @LocalServerPort
    private int port;

    private WebDriver webDriver;

    private LoginPage loginPage;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TravelGroupRepository travelGroupRepository;

    @Autowired
    private TravelGroupCommentRepository travelGroupCommentRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    private User user;

    private TravelGroup travelGroup;

    private static final String RAW_PASSWORD = "Test123";

    @BeforeEach
    public void setUp() {
        webDriver = new ChromeDriver();
        webDriver.manage().window().maximize();

        user = UserFixture.createEnabledUser(encoder.encode(RAW_PASSWORD));
        userRepository.save(user);

        User organiser = UserFixture.createWithAllFieldsWithDifferentUsernameAndEmail();
        organiser.setPassword(encoder.encode(RAW_PASSWORD));
        userRepository.save(organiser);

        Tournament tournament = TournamentFixture.createPublishedWithoutIdAndOrganiser(organiser);
        tournamentRepository.save(tournament);

        travelGroup = TravelGroupFixture.createWithoutIdAndUserAndTournament(user, tournament);
        travelGroupRepository.save(travelGroup);

        loginPage = new LoginPage(webDriver, port);
    }

    @AfterEach
    public void tearDown() {
        if (webDriver != null) {
            webDriver.quit();
        }

        travelGroupCommentRepository.deleteAll();
        travelGroupRepository.deleteAll();
        tournamentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void commentPage_shouldCreateComment_withValidData() {
        try {
            loginPage.login(user.getUsername(), RAW_PASSWORD);

            webDriver.get("http://localhost:" + port + "/travel-groups/" + travelGroup.getId() + "/comments");

            WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(5));

            webDriver.findElement(By.id("content")).sendKeys("TestContent");

            WebElement submitButton = webDriverWait.until(
                    ExpectedConditions.elementToBeClickable(By.cssSelector("button.button-primary.button-full")));

            ((JavascriptExecutor) webDriver).executeScript(
                    "arguments[0].scrollIntoView({block: 'center'});",
                    submitButton
            );

            new Actions(webDriver)
                    .moveToElement(submitButton)
                    .click()
                    .perform();

            WebElement displayedComment = webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                    "//p[contains(@class,'comment-content') and text()='TestContent']")));

            assertEquals(1, travelGroupCommentRepository.count());

            TravelGroupComment result = travelGroupCommentRepository.findAll().get(0);

            assertEquals("TestContent", displayedComment.getText());
            assertEquals(user.getId(), result.getAuthor().getId());
        } catch (Throwable e) {
            Utils.takeScreenshot(webDriver, "createComment");
            throw e;
        }
    }

    @Test
    public void commentPage_shouldNotCreateComment_withInvalidData(){
        try {
            loginPage.login(user.getUsername(), RAW_PASSWORD);

            webDriver.get("http://localhost:" + port + "/travel-groups/" + travelGroup.getId() + "/comments");

            WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(5));

            WebElement submitButton = webDriverWait.until(
                    ExpectedConditions.elementToBeClickable(By.cssSelector("button.button-primary.button-full")));

            ((JavascriptExecutor) webDriver).executeScript(
                    "arguments[0].scrollIntoView({block: 'center'});",
                    submitButton);

            new Actions(webDriver)
                    .moveToElement(submitButton)
                    .click()
                    .perform();

            webDriverWait.until(ExpectedConditions.stalenessOf(submitButton));

            WebElement errorMessage = webDriverWait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector("#content + p.field-error")));

            assertEquals(0, travelGroupCommentRepository.count());
            assertEquals("Comment content is required", errorMessage.getText());

        } catch (Throwable e) {
            Utils.takeScreenshot(webDriver, "createCommentWithInvalidData");
            throw e;
        }
    }
}
