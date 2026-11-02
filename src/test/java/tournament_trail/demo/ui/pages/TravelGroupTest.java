package tournament_trail.demo.ui.pages;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import tournament_trail.demo.entities.Tournament;
import tournament_trail.demo.entities.User;
import tournament_trail.demo.entities.enums.Role;
import tournament_trail.demo.fixtures.TournamentFixture;
import tournament_trail.demo.fixtures.UserFixture;
import tournament_trail.demo.repositories.TournamentRepository;
import tournament_trail.demo.repositories.UserRepository;

@SpringBootTest()
@ActiveProfiles("test")
public class TravelGroupTest {
    private WebDriver webDriver;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    private static final String RAW_PASSWORD = "Test123";

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();

        options.addArguments("--headless=new");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--force-device-scale-factor=1");
        options.addArguments("--disable-dev-shm-usage");

        webDriver = new ChromeDriver(options);
        
        User user = UserFixture.createEnabledUser();
        user.setPassword(encoder.encode(RAW_PASSWORD));
        userRepository.save(user);

        User organiser = UserFixture.createWithAllFieldsWithDifferentUsernameAndEmail();
        organiser.setPassword(encoder.encode(RAW_PASSWORD));
        organiser.setRole(Role.ORGANISER);
        organiser.setEnabled(true);
        userRepository.save(organiser);

        Tournament tournament = TournamentFixture.createWithoutIdAndOrganiser(organiser);
        tournamentRepository.save(tournament);
    }

    @AfterEach
    public void tearDown() {
        tournamentRepository.deleteAll();
        userRepository.deleteAll();
        if (webDriver != null) {
            webDriver.quit();
        }
    }


}
