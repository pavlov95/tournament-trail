package tournament_trail.demo.ui.pages;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
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
        webDriver = new ChromeDriver();
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
