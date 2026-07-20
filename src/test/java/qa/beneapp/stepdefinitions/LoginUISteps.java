package qa.beneapp.stepdefinitions;

import static org.junit.Assert.assertTrue;

import qa.beneapp.pages.bene.LoginPage;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Step definitions for LoginUI.feature (UI flow via Playwright POM).
 */
public class LoginUISteps {

    private final LoginPage loginPage = new LoginPage();

    @Given("I am on the bene-app login page")
    public void iAmOnTheLoginPage() {
        loginPage.open();
        assertTrue("Login page should be displayed", loginPage.isOnLoginPage());
    }

    @When("I log in with username {string} and password {string}")
    public void iLogInWith(String username, String password) {
        loginPage.login(username, password);
    }

    @Then("I should land on my dashboard")
    public void iShouldLandOnMyDashboard() {
        assertTrue("Dashboard should be shown after login", loginPage.isLoggedIn());
    }

    @When("I log out")
    public void iLogOut() {
        loginPage.logout();
    }

    @Then("I should be back on the login page")
    public void iShouldBeBackOnTheLoginPage() {
        assertTrue("Should be back on the login page", loginPage.isOnLoginPage());
    }

    @Then("I should see a login error containing {string}")
    public void iShouldSeeALoginErrorContaining(String expected) {
        String actual = loginPage.getErrorMessage();   // waits for the error to render
        assertTrue("Expected error containing '" + expected + "' but was: '" + actual + "'",
                actual.contains(expected));
    }
}
