package qa.beneapp.stepdefinitions;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import qa.beneapp.config.ConfigManager;
import qa.beneapp.utils.ApiUtil;
import qa.beneapp.utils.DBUtil;

/**
 * Step definitions for AccountApi.feature - a straightforward REST + DB test:
 * set up data, call the API, then assert the status, the response body and the
 * database. No page objects and no hooks - just RestAssured (via ApiUtil), JDBC
 * (via DBUtil) and small reusable assertion methods.
 *
 * The endpoint path comes from config; ApiUtil supplies base URL + auth header.
 */
public class AccountApiSteps {

    private static final String TABLE = "bene_schema.bene_accounts";
    private final String accountsPath = ConfigManager.get("api.accounts.path");

    private Map<String, String> submitted;   // the feature row = source of truth
    private Long accountId;                   // id of the account under test
    private Response response;

    // ============================ GIVEN (arrange) ============================
    @Given("no account exists with number {string}")
    public void noAccountExistsWithNumber(String accountNumber) throws Exception {
        DBUtil.update("DELETE FROM " + TABLE + " WHERE account_number = '" + accountNumber + "'");
    }

    @Given("an account exists in the database with:")
    public void anAccountExistsInTheDatabaseWith(DataTable dataTable) throws Exception {
        submitted = dataTable.asMaps().get(0);
        // Clean any leftover row, then seed exactly this account and keep its id.
        DBUtil.update("DELETE FROM " + TABLE + " WHERE account_number = '" + submitted.get("accountNumber")
                + "' OR email = '" + submitted.get("email") + "'");
        List<Map<String, Object>> inserted = DBUtil.query(
                "INSERT INTO " + TABLE
                + " (account_number, account_holder_name, email, date_of_birth, ssn_last_four, total_balance, status) "
                + "VALUES (" + values(submitted) + ") RETURNING id");
        accountId = ((Number) inserted.get(0).get("id")).longValue();
    }

    // ============================ WHEN (act) ============================
    @When("I create an account via the API with:")
    public void iCreateAnAccountViaTheApiWith(DataTable dataTable) {
        submitted = dataTable.asMaps().get(0);
        String body = accountJson(submitted);
        response = ApiUtil.post(accountsPath, body);
        if (response.getStatusCode() < 300) {
            accountId = response.jsonPath().getLong("id");
        }
    }

    @When("I get the account by its id via the API")
    public void iGetTheAccountByItsIdViaTheApi() {
        response = ApiUtil.get(accountsPath + "/" + accountId);
    }

    @When("I get the account by id {string} via the API")
    public void iGetTheAccountByIdViaTheApi(String id) {
        response = ApiUtil.get(accountsPath + "/" + id);
    }

    @When("I update the account via the API with:")
    public void iUpdateTheAccountViaTheApiWith(DataTable dataTable) {
        submitted = dataTable.asMaps().get(0);
        response = ApiUtil.put(accountsPath + "/" + accountId, accountJson(submitted));
    }

    @When("I delete the account via the API")
    public void iDeleteTheAccountViaTheApi() {
        response = ApiUtil.delete(accountsPath + "/" + accountId);
    }

    // ============================ THEN (assert) ============================
    @Then("the account API response status should be {int}")
    public void theAccountApiResponseStatusShouldBe(int expected) {
        assertEquals("Unexpected HTTP status", expected, response.getStatusCode());
    }

    @Then("the account API response should match the submitted data")
    public void theAccountApiResponseShouldMatchTheSubmittedData() {
        assertResponseMatches(response, submitted);
    }

    @Then("the account stored in the database should match the submitted data")
    public void theAccountStoredInTheDatabaseShouldMatchTheSubmittedData() throws Exception {
        Map<String, Object> db = fetchAccountRow(submitted.get("accountNumber"));
        assertDbMatches(db, submitted);
    }

    @Then("the account API response should equal the database record")
    public void theAccountApiResponseShouldEqualTheDatabaseRecord() throws Exception {
        // Reusable API-vs-DB check: read the DB row as a List<Map> and compare it
        // field-by-field against the JSON the API returned.
        Map<String, Object> db = fetchAccountRow(submitted.get("accountNumber"));
        assertApiEqualsDb(response, db);
    }

    @Then("the account in the database should have status {string}")
    public void theAccountInTheDatabaseShouldHaveStatus(String expectedStatus) throws Exception {
        List<Map<String, Object>> rows = DBUtil.query(
                "SELECT status FROM " + TABLE + " WHERE id = " + accountId);
        assertEquals("Account row should still exist after soft delete", 1, rows.size());
        assertEquals("Soft-deleted account status", expectedStatus, String.valueOf(rows.get(0).get("status")));
    }

    @Then("only one account should exist in the database with number {string}")
    public void onlyOneAccountShouldExistWithNumber(String accountNumber) throws Exception {
        List<Map<String, Object>> rows = DBUtil.query(
                "SELECT id FROM " + TABLE + " WHERE account_number = '" + accountNumber + "'");
        assertEquals("A duplicate must not be created", 1, rows.size());
    }

    // ============================ reusable helpers ============================
    /** Compare the API JSON body against the expected data from the feature. */
    private void assertResponseMatches(Response r, Map<String, String> e) {
        assertEquals("accountNumber", e.get("accountNumber"), r.jsonPath().getString("accountNumber"));
        assertEquals("accountHolderName", e.get("accountHolderName"), r.jsonPath().getString("accountHolderName"));
        assertEquals("email", e.get("email"), r.jsonPath().getString("email"));
        assertEquals("status", e.get("status"), r.jsonPath().getString("status"));
        assertEquals("totalBalance", Double.parseDouble(e.get("totalBalance")), r.jsonPath().getDouble("totalBalance"), 0.001);
    }

    /** Compare the DB row against the expected data from the feature. */
    private void assertDbMatches(Map<String, Object> db, Map<String, String> e) {
        assertEquals("account_holder_name", e.get("accountHolderName"), String.valueOf(db.get("account_holder_name")));
        assertEquals("email", e.get("email"), String.valueOf(db.get("email")));
        assertEquals("status", e.get("status"), String.valueOf(db.get("status")));
        assertEquals("total_balance", Double.parseDouble(e.get("totalBalance")),
                Double.parseDouble(String.valueOf(db.get("total_balance"))), 0.001);
    }

    /** Compare the API JSON body against the DB row (both describe the same account). */
    private void assertApiEqualsDb(Response r, Map<String, Object> db) {
        assertEquals("accountNumber", String.valueOf(db.get("account_number")), r.jsonPath().getString("accountNumber"));
        assertEquals("accountHolderName", String.valueOf(db.get("account_holder_name")), r.jsonPath().getString("accountHolderName"));
        assertEquals("email", String.valueOf(db.get("email")), r.jsonPath().getString("email"));
        assertEquals("status", String.valueOf(db.get("status")), r.jsonPath().getString("status"));
        assertEquals("totalBalance", Double.parseDouble(String.valueOf(db.get("total_balance"))),
                r.jsonPath().getDouble("totalBalance"), 0.001);
    }

    private Map<String, Object> fetchAccountRow(String accountNumber) throws Exception {
        List<Map<String, Object>> rows = DBUtil.query(
                "SELECT account_number, account_holder_name, email, total_balance, status "
                + "FROM " + TABLE + " WHERE account_number = '" + accountNumber + "'");
        assertEquals("Exactly one DB row expected for " + accountNumber, 1, rows.size());
        return rows.get(0);
    }

    /** VALUES(...) list for the seed insert, in column order. */
    private String values(Map<String, String> d) {
        return "'" + d.get("accountNumber") + "', '" + d.get("accountHolderName") + "', '" + d.get("email") + "', '"
                + d.get("dateOfBirth") + "', '" + d.get("ssnLastFour") + "', " + d.get("totalBalance")
                + ", '" + d.get("status") + "'";
    }

    /** JSON request body built from the feature data. */
    private String accountJson(Map<String, String> d) {
        return "{"
                + "\"accountNumber\":\"" + d.get("accountNumber") + "\","
                + "\"accountHolderName\":\"" + d.get("accountHolderName") + "\","
                + "\"email\":\"" + d.get("email") + "\","
                + "\"dateOfBirth\":\"" + d.get("dateOfBirth") + "\","
                + "\"ssnLastFour\":\"" + d.get("ssnLastFour") + "\","
                + "\"totalBalance\":" + d.get("totalBalance") + ","
                + "\"status\":\"" + d.get("status") + "\"}";
    }
}
