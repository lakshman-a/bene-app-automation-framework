package qa.beneapp.stepdefinitions;

import static org.junit.Assert.assertTrue;

import com.microsoft.playwright.Page;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import qa.beneapp.driver.PlaywrightDriver;

/**
 * Step definitions for the @public UI scenario in PublicSmoke.feature.
 * Uses the-internet.herokuapp.com (a stable public practice site) so this
 * scenario passes without the bene-app running - useful for proving the
 * Playwright + Jenkins pipeline works end to end.
 */
public class PublicUiSteps {

    private Page page() {
        return PlaywrightDriver.getPage();
    }

    @Given("I am on the public practice login page")
    public void iAmOnThePublicPracticeLoginPage() {
        page().navigate("https://the-internet.herokuapp.com/login");
    }

    @When("I log in to the practice site with username {string} and password {string}")
    public void iLogInToThePracticeSiteWith(String username, String password) {
        page().locator("#username").fill(username);
        page().locator("#password").fill(password);
        page().locator("button[type='submit']").click();
    }

    @Then("I should see the secure area message")
    public void iShouldSeeTheSecureAreaMessage() {
        String flash = page().locator("#flash").textContent();
        assertTrue("Expected secure area message but was: " + flash,
                flash.contains("You logged into a secure area"));
    }
}
