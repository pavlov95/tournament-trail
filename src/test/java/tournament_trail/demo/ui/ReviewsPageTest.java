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
import tournament_trail.demo.entities.Review;
import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.TournamentRegistration;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.enums.Rating;
import tournament_trail.demo.entities.enums.Role;
import tournament_trail.demo.fixtures.*;
import tournament_trail.demo.repositories.ReviewRepository;
import tournament_trail.demo.repositories.TournamentRegistrationRepository;
import tournament_trail.demo.repositories.TournamentRepository;
import tournament_trail.demo.repositories.UserRepository;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import tournament_trail.demo.ui.pages.LoginPage;


@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReviewsPageTest {

    @LocalServerPort
    private int port;

    private WebDriver webDriver;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TournamentRegistrationRepository tournamentRegistrationRepository;

    private static final String RAW_PASSWORD = "Test123";

    private User user;

    private Tournament tournament;

    private LoginPage loginPage;

    @BeforeEach
    public void setUp() {
        webDriver = new ChromeDriver();
        user = UserFixture.createEnabledUser(encoder.encode(RAW_PASSWORD));
        userRepository.save(user);

        User organiser = UserFixture.createWithAllFieldsWithDifferentUsernameAndEmail();
        organiser.setRole(Role.ORGANISER);
        userRepository.save(organiser);

        tournament = TournamentFixture.createWithoutIdAndOrganiser(organiser);
        tournamentRepository.save(tournament);

        TournamentRegistration tournamentRegistration = TournamentRegistrationFixture.create(user, tournament);
        tournamentRegistrationRepository.save(tournamentRegistration);

        loginPage = new LoginPage(webDriver, port);
    }

    @AfterEach
    public void tearDown() {
        if (webDriver != null) {
            webDriver.quit();
        }
        reviewRepository.deleteAll();
        tournamentRegistrationRepository.deleteAll();
        tournamentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void reviewsPage_shouldCreateReview_withValidData() {
        try {
            loginPage.login(user.getUsername(), RAW_PASSWORD);
            WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(5));

            webDriver.get("http://localhost:" + port + "/tournaments/" + tournament.getId() + "/reviews");

            new Select(webDriver.findElement(By.id("rating"))).selectByValue(Rating.GOOD.name());
            webDriver.findElement(By.id("title")).sendKeys("Test");
            webDriver.findElement(By.id("content")).sendKeys("Testcontent");
            webDriver.findElement(By.cssSelector("button.button-primary.full-button")).click();

            webDriverWait.until(driver -> driver.findElements(By.cssSelector(".review-item")).size() == 1);

            assertEquals(1, reviewRepository.count());
            Review result = reviewRepository.findAll().get(0);
            assertEquals(user.getId(), result.getAuthor().getId());
            assertEquals(tournament.getId(), result.getTournament().getId());

        } catch (Throwable e) {
            Utils.takeScreenshot(webDriver, "creatingReview");
            throw e;
        }
    }

    @Test
    public void reviewsPage_shouldNotCreateReview_withInvalidData() {
        try {
           loginPage.login(user.getUsername(), RAW_PASSWORD);

            WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(5));

            webDriver.get("http://localhost:" + port + "/tournaments/" + tournament.getId() + "/reviews");

            webDriver.findElement(By.id("title")).sendKeys("TestTitle");
            webDriver.findElement(By.id("content")).sendKeys("Test");
            webDriver.findElement(By.cssSelector("button.button-primary.full-button")).click();

            WebElement ratingError = webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath
                    ("//select[@id='rating']/following-sibling::*[contains(@class,'field-error')]")));

            assertEquals("You must choose a rating.", ratingError.getText());
            assertEquals(0, reviewRepository.count());

        } catch (Throwable e) {
            Utils.takeScreenshot(webDriver, "creatingReviewWithInvalidData");
            throw e;
        }
    }

    @Test
    public void reviewsPage_shouldEditReview_withValidData() {
        try {
            Review review = ReviewFixture.create(user, tournament);
            reviewRepository.save(review);

           loginPage.login(user.getUsername(), RAW_PASSWORD);

            WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(5));

            webDriver.get("http://localhost:" + port + "/tournaments/" + tournament.getId() + "/reviews");

            webDriver.findElement(By.cssSelector("details.edit-review summary")).click();

            WebElement ratingSelect = webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("edit-rating-" + review.getId())));
            new Select(ratingSelect).selectByValue(Rating.EXCELLENT.name());

            WebElement titleInput = webDriver.findElement(By.id("edit-title-" + review.getId()));
            titleInput.clear();
            titleInput.sendKeys("TestEditTitle");

            WebElement contentInput = webDriver.findElement(By.id("edit-content-" + review.getId()));
            contentInput.clear();
            contentInput.sendKeys("TestEditContent");

            webDriver.findElement(By.cssSelector(".inline-review-form button[type='submit']")).click();

            webDriverWait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.xpath(
                            "//article[contains(@class,'review-item')]//h4[text()='TestEditTitle']")));

            assertEquals(1, reviewRepository.count());
            Review result = reviewRepository.findById(review.getId()).orElseThrow();

            assertEquals(Rating.EXCELLENT, result.getRating());
            assertEquals("TestEditTitle", result.getTitle());
            assertEquals("TestEditContent", result.getContent());
        } catch (Throwable e) {
            Utils.takeScreenshot(webDriver, "editReview");
            throw e;
        }
    }

    @Test
    public void reviewsPage_shouldNotEditReview_withInvalidData() {
        try {
            Review review = ReviewFixture.create(user, tournament);
            reviewRepository.save(review);

            loginPage.login(user.getUsername(), RAW_PASSWORD);

            WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(5));

            webDriver.get("http://localhost:" + port + "/tournaments/" + tournament.getId() + "/reviews");

            webDriver.findElement(By.cssSelector("details.edit-review summary")).click();

            WebElement ratingSelect = webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("edit-rating-" + review.getId())));
            new Select(ratingSelect).selectByValue(Rating.EXCELLENT.name());

            WebElement titleInput = webDriver.findElement(By.id("edit-title-" + review.getId()));
            titleInput.clear();
            titleInput.sendKeys("TestEditTitle");

            WebElement contentInput = webDriver.findElement(By.id("edit-content-" + review.getId()));
            contentInput.clear();

            WebElement submitButton = webDriver.findElement(By.cssSelector(".inline-review-form button[type='submit']"));
            submitButton.click();

            webDriverWait.until(ExpectedConditions.stalenessOf(submitButton));

            assertEquals(1, reviewRepository.count());
            Review result = reviewRepository.findById(review.getId()).orElseThrow();

            assertEquals(review.getRating(), result.getRating());
            assertEquals(review.getContent(), result.getContent());
            assertEquals(review.getTitle(), result.getTitle());
        } catch (Throwable e) {
            Utils.takeScreenshot(webDriver, "editReviewWithInvalidData");
            throw e;
        }
    }

    @Test
    public void reviewsPage_shouldDeleteReview() {
        try {
            Review review = ReviewFixture.create(user, tournament);
            reviewRepository.save(review);

           loginPage.login(user.getUsername(), RAW_PASSWORD);

            WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(5));

            webDriver.get("http://localhost:" + port + "/tournaments/" + tournament.getId() + "/reviews");

            WebElement reviewElement = webDriver.findElement(By.cssSelector(".review-item"));

            webDriver.findElement(By.cssSelector(".text-danger-button")).click();

            webDriverWait.until(ExpectedConditions.alertIsPresent()).accept();
            webDriverWait.until(ExpectedConditions.stalenessOf(reviewElement));
            assertTrue(reviewRepository.findById(review.getId()).isEmpty());

        } catch (Throwable e) {
            Utils.takeScreenshot(webDriver, "deleteReview");
            throw e;
        }
    }


}