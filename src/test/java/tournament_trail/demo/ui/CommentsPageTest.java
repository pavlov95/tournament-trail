package tournament_trail.demo.ui;

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
import tournament_trail.demo.fixtures.TravelGroupCommentFixture;
import tournament_trail.demo.fixtures.TravelGroupFixture;
import tournament_trail.demo.fixtures.UserFixture;
import tournament_trail.demo.repositories.TournamentRepository;
import tournament_trail.demo.repositories.TravelGroupCommentRepository;
import tournament_trail.demo.repositories.TravelGroupRepository;
import tournament_trail.demo.repositories.UserRepository;
import tournament_trail.demo.ui.pages.LoginPage;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

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

            WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(10));

            WebElement commentText = webDriver.findElement(By.id("content"));

            commentText.sendKeys("TestContent");

            WebElement commentForm = webDriver.findElement(By.cssSelector("form.comment-form"));

            WebElement submitButton = commentForm.findElement(By.cssSelector("button[type='submit']"));

            ((JavascriptExecutor) webDriver).executeScript(
                    "arguments[0].requestSubmit(arguments[1]);", commentForm, submitButton);

            WebElement displayedComment = webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//p[contains(@class,'comment-content') and text()='TestContent']")));

            TravelGroupComment result = travelGroupCommentRepository.findAll().get(0);

            assertEquals("TestContent", displayedComment.getText());

            assertEquals("TestContent", result.getContent());

            assertEquals(user.getId(), result.getAuthor().getId());

        } catch (Throwable e) {
            Utils.takeScreenshot(webDriver, "createComment");
            throw e;
        }
    }

    @Test
    public void commentPage_shouldNotCreateComment_withInvalidData() {
        try {
            loginPage.login(user.getUsername(), RAW_PASSWORD);

            webDriver.get("http://localhost:" + port + "/travel-groups/" + travelGroup.getId() + "/comments");

            WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(10));

            WebElement commentForm = webDriver.findElement(By.cssSelector("form.comment-form"));

            WebElement submitButton = commentForm.findElement(By.cssSelector("button[type='submit']"));

            ((JavascriptExecutor) webDriver).executeScript(
                    "arguments[0].requestSubmit(arguments[1]);", commentForm, submitButton);

            WebElement errorMessage = webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector("#content + p.field-error")));

            assertEquals("Comment content is required", errorMessage.getText());

            assertEquals(0, travelGroupCommentRepository.count());

        } catch (Throwable e) {
            Utils.takeScreenshot(webDriver, "createCommentWithInvalidData");
            throw e;
        }
    }

    @Test
    public void commentPage_shouldDeleteComment() {
        try {
            TravelGroupComment comment = TravelGroupCommentFixture.createWithAuthorAndTravelGroup(user, travelGroup);

            travelGroupCommentRepository.save(comment);

            loginPage.login(user.getUsername(), RAW_PASSWORD);

            webDriver.get("http://localhost:" + port + "/travel-groups/" + travelGroup.getId() + "/comments");

            WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(10));

            WebElement deleteButton = webDriver.findElement(By.cssSelector(".link-button.danger-link"));

            ((JavascriptExecutor) webDriver).executeScript(
                    "arguments[0].scrollIntoView({block: 'center'});", deleteButton);

            deleteButton.click();

            webDriverWait.until(ExpectedConditions.alertIsPresent()).accept();

            webDriverWait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".comment-card")));

            TravelGroupComment result = travelGroupCommentRepository.findById(comment.getId()).orElseThrow();

            assertTrue(result.isHidden());

        } catch (Throwable e) {
            Utils.takeScreenshot(webDriver, "deleteComment");
            throw e;
        }
    }

    @Test
    public void commentPage_shouldPinComment() {
        try {
            TravelGroupComment comment = TravelGroupCommentFixture.createWithAuthorAndTravelGroup(user, travelGroup);

            travelGroupCommentRepository.save(comment);

            loginPage.login(user.getUsername(), RAW_PASSWORD);

            webDriver.get("http://localhost:" + port + "/travel-groups/" + travelGroup.getId() + "/comments");

            WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(10));

            WebElement pinForm = webDriverWait.until(ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("form[action*='/pin']")));

            WebElement pinButton = pinForm.findElement(By.cssSelector("button.link-button"));

            ((JavascriptExecutor) webDriver).executeScript(
                    "arguments[0].requestSubmit(arguments[1]);", pinForm, pinButton);

            WebElement unpinButton = webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector("form[action*='/unpin'] button.link-button")));

            WebElement updatedCommentCard = webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector(".comment-card")));

            assertTrue(updatedCommentCard.getAttribute("class").contains("pinned-comment"));

            TravelGroupComment result = travelGroupCommentRepository.findById(comment.getId()).orElseThrow();

            assertTrue(result.isPinned());

        } catch (Throwable e) {
            Utils.takeScreenshot(webDriver, "pinComment");
            throw e;
        }
    }

    @Test
    public void commentPage_shouldUnpinComment(){
        try {
            TravelGroupComment comment = TravelGroupCommentFixture.createWithAuthorAndTravelGroup(user, travelGroup);
            comment.setPinned(true);
            travelGroupCommentRepository.save(comment);

            loginPage.login(user.getUsername(), RAW_PASSWORD);

            webDriver.get("http://localhost:" + port + "/travel-groups/" + travelGroup.getId() + "/comments");

            WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(10));

            WebElement commentCard = webDriver.findElement(By.cssSelector(".comment-card"));

            WebElement unpinButton = webDriver.findElement(By.cssSelector("form[action*='/unpin'] button.link-button"));
            unpinButton.click();

            webDriverWait.until(ExpectedConditions.stalenessOf(commentCard));

            WebElement updatedCommentCard = webDriverWait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".comment-card")));

            TravelGroupComment result = travelGroupCommentRepository.findById(comment.getId()).orElseThrow();
            assertFalse(result.isPinned());
        } catch (Throwable e) {
            Utils.takeScreenshot(webDriver, "unpinComment");
            throw e;
        }
    }

    @Test
    public void commentPage_shouldEditComment_withValidData() {
        try {
            TravelGroupComment comment = TravelGroupCommentFixture.createWithAuthorAndTravelGroup(user, travelGroup);

            travelGroupCommentRepository.save(comment);

            loginPage.login(user.getUsername(), RAW_PASSWORD);

            webDriver.get("http://localhost:" + port + "/travel-groups/" + travelGroup.getId() + "/comments");

            WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(10));

            WebElement editDetails = webDriver.findElement(By.cssSelector("details.edit-details"));

            ((JavascriptExecutor) webDriver).executeScript(
                    "arguments[0].open = true;", editDetails);

            WebElement commentText = webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector("form.edit-comment-form textarea")));

            commentText.clear();
            commentText.sendKeys("EditCommentTest");

            WebElement editForm = webDriver.findElement(By.cssSelector("form.edit-comment-form"));

            WebElement submitButton = editForm.findElement(By.cssSelector("button[type='submit']"));

            ((JavascriptExecutor) webDriver).executeScript(
                    "arguments[0].requestSubmit(arguments[1]);", editForm, submitButton);

            WebElement displayedComment = webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//p[contains(@class,'comment-content') and text()='EditCommentTest']")));

            TravelGroupComment result = travelGroupCommentRepository.findById(comment.getId()).orElseThrow();

            assertEquals("EditCommentTest", displayedComment.getText());

            assertEquals("EditCommentTest", result.getContent());

            assertEquals(user.getId(), result.getAuthor().getId());

        } catch (Throwable e) {
            Utils.takeScreenshot(webDriver, "editComment");
            throw e;
        }
    }

    @Test
    public void commentPage_shouldNotEditComment_withInvalidData() {
        try {
            TravelGroupComment comment = TravelGroupCommentFixture.createWithAuthorAndTravelGroup(user, travelGroup);

            travelGroupCommentRepository.save(comment);

            loginPage.login(user.getUsername(), RAW_PASSWORD);

            webDriver.get("http://localhost:" + port + "/travel-groups/" + travelGroup.getId() + "/comments");

            WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(10));

            WebElement editDetails = webDriver.findElement(By.cssSelector("details.edit-details"));

            ((JavascriptExecutor) webDriver).executeScript(
                    "arguments[0].open = true;", editDetails);

            WebElement commentText = webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector("form.edit-comment-form textarea")));

            commentText.clear();

            WebElement submitButton = webDriver.findElement(By.cssSelector("form.edit-comment-form button"));

            ((JavascriptExecutor) webDriver).executeScript(
                    "arguments[0].scrollIntoView({block: 'center'});", submitButton);

            new Actions(webDriver)
                    .moveToElement(submitButton)
                    .click()
                    .perform();

            TravelGroupComment result = travelGroupCommentRepository.findById(comment.getId()).orElseThrow();

            assertEquals(comment.getContent(), result.getContent());
            assertEquals(user.getId(), result.getAuthor().getId());

        } catch (Throwable e) {
            Utils.takeScreenshot(webDriver, "editCommentWithInvalidData");
            throw e;
        }
    }
}