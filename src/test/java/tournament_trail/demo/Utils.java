package tournament_trail.demo;

import org.openqa.selenium.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {
    public static void takeScreenshot(WebDriver driver, String fileName) {
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        Path destination = Path.of("target", "screenshots", fileName + ".png");

        try {
            Files.createDirectories(destination.getParent());

            Files.copy(screenshot.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            throw new RuntimeException("Could not save screenshot", e);
        }
    }

    public static void setDateTimeLocal(WebDriver webDriver, String elementId, LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        String value = dateTime.format(formatter);

        WebElement element = webDriver.findElement(By.id(elementId));

        ((JavascriptExecutor) webDriver).executeScript(
                """
                arguments[0].value = arguments[1];
                arguments[0].dispatchEvent(
                    new Event('input', { bubbles: true })
                );
                arguments[0].dispatchEvent(
                    new Event('change', { bubbles: true })
                );
                """,
                element,
                value
        );
    }
}
