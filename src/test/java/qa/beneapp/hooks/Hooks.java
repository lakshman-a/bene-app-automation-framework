package qa.beneapp.hooks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qa.beneapp.driver.PlaywrightDriver;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

/**
 * Hooks - Cucumber lifecycle management.
 *
 * The Playwright browser starts only for UI scenarios (tagged @ui), so API/DB
 * scenarios run without a browser.
 *
 * Per-step screenshots are handled by {@link ScreenshotStepListener} (registered
 * in TestRunner), which attaches them inline under each step in Allure.
 */
public class Hooks {

    private static final Logger log = LoggerFactory.getLogger(Hooks.class);

    private boolean isUi(Scenario scenario) {
        return scenario.getSourceTagNames().contains("@ui");
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        log.info("==================================================");
        log.info("STARTING SCENARIO: {}", scenario.getName());
        log.info("TAGS: {}", scenario.getSourceTagNames());
        log.info("==================================================");

        if (isUi(scenario)) {
            PlaywrightDriver.initDriver();
        }
    }

    @After
    public void afterScenario(Scenario scenario) {
        log.info("SCENARIO STATUS: {} - {}", scenario.getName(), scenario.getStatus());
        if (isUi(scenario)) {
            PlaywrightDriver.quitDriver();
        }
        log.info("FINISHED SCENARIO: {}\n", scenario.getName());
    }
}
