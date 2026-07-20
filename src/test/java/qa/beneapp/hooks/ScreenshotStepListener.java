package qa.beneapp.hooks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestStepFinished;
import io.qameta.allure.Allure;
import qa.beneapp.config.ConfigManager;
import qa.beneapp.driver.PlaywrightDriver;

/**
 * Attaches a Playwright screenshot to EACH Gherkin step in the Allure report.
 *
 * It listens for {@code TestStepFinished} and is registered BEFORE the Allure
 * plugin in TestRunner, so it runs while the Allure step is still open - which
 * makes the screenshot appear inline under that step (not bunched in tear-down,
 * which is what an @AfterStep hook would do).
 *
 * Behaviour is config-driven via {@code screenshot.on.each.step}:
 *   true  -> attach after every step
 *   false -> attach only after a failed step (default)
 * Screenshots are only taken for UI scenarios (when a browser page is active).
 */
public class ScreenshotStepListener implements ConcurrentEventListener {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotStepListener.class);

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepFinished.class, this::onStepFinished);
    }

    private void onStepFinished(TestStepFinished event) {
        // Only real Gherkin steps (skip Before/After hook steps).
        if (!(event.getTestStep() instanceof PickleStepTestStep)) {
            return;
        }
        // Only when a UI browser is active (skips @api / @db scenarios).
        if (!PlaywrightDriver.hasPage()) {
            return;
        }
        boolean eachStep = ConfigManager.getBoolean("screenshot.on.each.step", false);
        boolean failed = event.getResult().getStatus() != Status.PASSED;
        if (!eachStep && !failed) {
            return;
        }

        try {
            byte[] shot = PlaywrightDriver.takeScreenshot();
            if (shot.length > 0) {
                PickleStepTestStep step = (PickleStepTestStep) event.getTestStep();
                String name = step.getStep().getKeyword().trim() + " " + step.getStep().getText();
                Allure.getLifecycle().addAttachment(name, "image/png", "png", shot);
            }
        } catch (Exception e) {
            log.warn("Could not attach step screenshot: {}", e.getMessage());
        }
    }
}
