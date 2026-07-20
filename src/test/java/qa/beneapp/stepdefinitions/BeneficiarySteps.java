package qa.beneapp.stepdefinitions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import qa.beneapp.pages.bene.BeneficiaryPage;
import qa.beneapp.pages.bene.DashboardPage;
import qa.beneapp.utils.DBUtil;

/**
 * Step definitions for BeneficiaryUI.feature.
 *
 * The scenario uses an EXISTING account (it does not create one) and proves the
 * end-to-end path: a beneficiary added through the UI is reflected in the UI and
 * persisted in the database. Thorough field/business-rule checks belong to the
 * API test layer, not here. No @After lives here - browser teardown is handled
 * by Hooks, and repeatability comes from the Background cleanup step.
 */
public class BeneficiarySteps {

    private static final Logger log = LoggerFactory.getLogger(BeneficiarySteps.class);

    private final DashboardPage dashboardPage = new DashboardPage();
    private final BeneficiaryPage beneficiaryPage = new BeneficiaryPage();

    private Long accountId;

    @Given("existing beneficiaries are cleared for account {string}")
    public void existingBeneficiariesAreClearedForAccount(String accountNumber) throws Exception {
        // Resolve the account id; the scenario fails fast if the account is absent.
        List<Map<String, Object>> rows = DBUtil.query(
                "SELECT id FROM bene_schema.bene_accounts WHERE account_number = '" + accountNumber + "'");
        assertEquals("Test account '" + accountNumber + "' must already exist in the DB", 1, rows.size());
        accountId = ((Number) rows.get(0).get("id")).longValue();

        int cleared = DBUtil.update("DELETE FROM bene_schema.beneficiaries WHERE account_id = " + accountId);
        log.info("Background cleanup: cleared {} existing beneficiaries for account {} (id={})",
                cleared, accountNumber, accountId);
    }

    @Given("my 401k dashboard shows account {string}")
    public void my401kDashboardShowsAccount(String expectedAccountNumber) {
        assertTrue("Dashboard home should load", dashboardPage.isLoaded());
        dashboardPage.waitForMyAccount();
        assertEquals("Active account number", expectedAccountNumber, dashboardPage.getActiveAccountNumber());
    }

    @When("I open the Beneficiaries page")
    public void iOpenTheBeneficiariesPage() {
        dashboardPage.openBeneficiaries();
        assertTrue("Beneficiaries page should load", beneficiaryPage.isLoaded());
    }

    @When("I add a beneficiary:")
    public void iAddABeneficiary(DataTable dataTable) {
        Map<String, String> row = dataTable.asMaps().get(0);
        beneficiaryPage.addBeneficiary(row.get("name"), row.get("relationship"), row.get("dob"), row.get("percentage"));
    }

    @Then("the beneficiary {string} should be listed in the table")
    public void theBeneficiaryShouldBeListed(String name) {
        assertTrue("Beneficiary '" + name + "' should be in the table", beneficiaryPage.isBeneficiaryListed(name));
    }

    @Then("the active allocation total should be {double}%")
    public void theActiveAllocationTotalShouldBe(double expected) {
        assertEquals("Active allocation total", expected, beneficiaryPage.getActiveTotalPercent(), 0.01);
    }

    @Then("the beneficiary {string} should be saved in the database with percentage {double}")
    public void theBeneficiaryShouldBeSavedInDb(String name, double expectedPct) throws Exception {
        // Confirms the UI action persisted through the API into the database.
        List<Map<String, Object>> rows = DBUtil.query(
                "SELECT beneficiary_name, percentage, status FROM bene_schema.beneficiaries "
                + "WHERE account_id = " + accountId + " AND beneficiary_name = '" + name + "'");
        assertEquals("Exactly one beneficiary row should be persisted", 1, rows.size());
        double dbPct = Double.parseDouble(String.valueOf(rows.get(0).get("percentage")));
        assertEquals("Persisted percentage", expectedPct, dbPct, 0.01);
    }

    // ---------- Negative path ----------
    @When("I try to add a beneficiary:")
    public void iTryToAddABeneficiary(DataTable dataTable) {
        Map<String, String> row = dataTable.asMaps().get(0);
        beneficiaryPage.tryAddBeneficiary(row.get("name"), row.get("relationship"), row.get("dob"), row.get("percentage"));
    }

    @Then("I should see an add-beneficiary error")
    public void iShouldSeeAnAddBeneficiaryError() {
        assertTrue("An error toast should be shown for the rejected beneficiary", beneficiaryPage.isAddErrorShown());
    }

    @Then("the beneficiary {string} should not be listed in the table")
    public void theBeneficiaryShouldNotBeListed(String name) {
        assertTrue("Beneficiary '" + name + "' must NOT appear in the table", !beneficiaryPage.isBeneficiaryListed(name));
    }

    @Then("the beneficiary {string} should not be saved in the database")
    public void theBeneficiaryShouldNotBeSavedInDb(String name) throws Exception {
        List<Map<String, Object>> rows = DBUtil.query(
                "SELECT id FROM bene_schema.beneficiaries "
                + "WHERE account_id = " + accountId + " AND beneficiary_name = '" + name + "'");
        assertEquals("A rejected beneficiary must not be persisted", 0, rows.size());
    }
}
