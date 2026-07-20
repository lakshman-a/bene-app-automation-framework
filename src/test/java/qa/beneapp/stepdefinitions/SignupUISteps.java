package qa.beneapp.stepdefinitions;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import qa.beneapp.pages.bene.DashboardPage;
import qa.beneapp.pages.bene.SignupPage;
import qa.beneapp.utils.DBUtil;

/**
 * Step definitions for SignupUI.feature.
 *
 * All sign-up data comes from the feature table. The user (order_schema.users)
 * and their auto-created account (bene_schema.bene_accounts) are removed by the
 * precondition step before the run, so the scenario is atomic and re-runnable.
 */
public class SignupUISteps {

    private final SignupPage signupPage = new SignupPage();
    private final DashboardPage dashboardPage = new DashboardPage();

    private String username;
    private String email;

    @Given("no user exists with username {string} and email {string}")
    public void noUserExistsWith(String username, String email) throws Exception {
        this.username = username;
        this.email = email;
        deleteUserAndAccount(username, email);
    }

    @When("I sign up as a new end user:")
    public void iSignUpAsANewEndUser(DataTable dataTable) {
        Map<String, String> row = dataTable.asMaps().get(0);
        this.username = row.get("username");
        this.email = row.get("email");
        signupPage.open();
        signupPage.signUp(
                row.get("fullName"), row.get("username"), row.get("email"), row.get("phone"),
                row.get("dob"), row.get("ssn"), row.get("balance"), row.get("password"));
    }

    @Then("I should be redirected to the login page")
    public void iShouldBeRedirectedToTheLoginPage() {
        assertTrue("Signup should redirect to login", signupPage.isRedirectedToLogin());
    }

    @Then("my 401k dashboard home should be visible")
    public void my401kDashboardHomeShouldBeVisible() {
        assertTrue("Dashboard home should be visible after login", dashboardPage.isLoaded());
    }

    @Then("I should see a signup error containing {string}")
    public void iShouldSeeASignupErrorContaining(String expected) {
        String actual = signupPage.getErrorMessage();   // waits for the error to render
        assertTrue("Expected signup error containing '" + expected + "' but was: '" + actual + "'",
                actual.contains(expected));
    }

    @Then("no account should exist in the database for email {string}")
    public void noAccountShouldExistForEmail(String email) throws Exception {
        List<Map<String, Object>> rows = DBUtil.query(
                "SELECT id FROM bene_schema.bene_accounts WHERE email = '" + email + "'");
        assertEquals("A rejected signup must not create an account row", 0, rows.size());
    }

    private void deleteUserAndAccount(String username, String email) throws Exception {
        DBUtil.update("DELETE FROM bene_schema.bene_accounts WHERE email = '" + email + "'");
        DBUtil.update("DELETE FROM order_schema.users WHERE username = '" + username + "'");
    }
}
