package qa.beneapp.stepdefinitions;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import qa.beneapp.config.ConfigManager;
import qa.beneapp.utils.ApiUtil;
import qa.beneapp.utils.DBUtil;

/**
 * Step definitions for BeneficiaryApi.feature - same clean pattern as the
 * accounts API: arrange data, call the API (RestAssured via ApiUtil), then assert
 * the status, the response body and the database (JDBC via DBUtil). No page
 * objects, no hooks. Endpoint path comes from config.
 */
public class BeneficiaryApiSteps {

    private static final String ACCOUNTS_TABLE = "bene_schema.bene_accounts";
    private static final String BENE_TABLE = "bene_schema.beneficiaries";
    private final String benePath = ConfigManager.get("api.beneficiaries.path");

    private Long accountId;                   // parent account
    private Long beneId;                      // beneficiary under test
    private Map<String, String> submitted;    // the feature row = source of truth
    private Response response;

    // ============================ GIVEN (arrange) ============================
    @Given("the account {string} has no beneficiaries")
    public void theAccountHasNoBeneficiaries(String accountNumber) throws Exception {
        accountId = resolveAccountId(accountNumber);
        DBUtil.update("DELETE FROM " + BENE_TABLE + " WHERE account_id = " + accountId);
    }

    @Given("the account {string} has a beneficiary:")
    public void theAccountHasABeneficiary(String accountNumber, DataTable dataTable) throws Exception {
        accountId = resolveAccountId(accountNumber);
        DBUtil.update("DELETE FROM " + BENE_TABLE + " WHERE account_id = " + accountId);
        submitted = dataTable.asMaps().get(0);
        List<Map<String, Object>> inserted = DBUtil.query(
                "INSERT INTO " + BENE_TABLE
                + " (account_id, beneficiary_name, relationship, date_of_birth, percentage, status) "
                + "VALUES (" + accountId + ", '" + submitted.get("beneficiaryName") + "', '"
                + submitted.get("relationship") + "', '" + submitted.get("dateOfBirth") + "', "
                + submitted.get("percentage") + ", '" + submitted.get("status") + "') RETURNING id");
        beneId = ((Number) inserted.get(0).get("id")).longValue();
    }

    // ============================ WHEN (act) ============================
    @When("I add a beneficiary via the API with:")
    public void iAddABeneficiaryViaTheApiWith(DataTable dataTable) {
        submitted = dataTable.asMaps().get(0);
        response = ApiUtil.post(benePath + "/account/" + accountId, beneJson(submitted));
        if (response.getStatusCode() < 300) {
            beneId = response.jsonPath().getLong("id");
        }
    }

    @When("I get the beneficiary by its id via the API")
    public void iGetTheBeneficiaryByItsIdViaTheApi() {
        response = ApiUtil.get(benePath + "/" + beneId);
    }

    @When("I get the beneficiary by id {string} via the API")
    public void iGetTheBeneficiaryByIdViaTheApi(String id) {
        response = ApiUtil.get(benePath + "/" + id);
    }

    @When("I update the beneficiary via the API with:")
    public void iUpdateTheBeneficiaryViaTheApiWith(DataTable dataTable) {
        submitted = dataTable.asMaps().get(0);
        response = ApiUtil.put(benePath + "/" + beneId, beneJson(submitted));
    }

    @When("I delete the beneficiary via the API")
    public void iDeleteTheBeneficiaryViaTheApi() {
        response = ApiUtil.delete(benePath + "/" + beneId);
    }

    // ============================ THEN (assert) ============================
    @Then("the beneficiary API response status should be {int}")
    public void theBeneficiaryApiResponseStatusShouldBe(int expected) {
        assertEquals("Unexpected HTTP status", expected, response.getStatusCode());
    }

    @Then("the beneficiary API response should match the submitted data")
    public void theBeneficiaryApiResponseShouldMatchTheSubmittedData() {
        assertResponseMatches(response, submitted);
    }

    @Then("the beneficiary stored in the database should match the submitted data")
    public void theBeneficiaryStoredInTheDatabaseShouldMatchTheSubmittedData() throws Exception {
        assertDbMatches(fetchBeneRow(beneId), submitted);
    }

    @Then("the beneficiary API response should equal the database record")
    public void theBeneficiaryApiResponseShouldEqualTheDatabaseRecord() throws Exception {
        // Reusable API-vs-DB check: read the DB row as a List<Map> and compare it
        // field-by-field against the JSON the API returned.
        assertApiEqualsDb(response, fetchBeneRow(beneId));
    }

    @Then("the beneficiary should not exist in the database")
    public void theBeneficiaryShouldNotExistInTheDatabase() throws Exception {
        List<Map<String, Object>> rows = DBUtil.query("SELECT id FROM " + BENE_TABLE + " WHERE id = " + beneId);
        assertEquals("A hard-deleted beneficiary must be gone", 0, rows.size());
    }

    @Then("the beneficiary {string} should not be stored in the database")
    public void theBeneficiaryShouldNotBeStored(String name) throws Exception {
        List<Map<String, Object>> rows = DBUtil.query(
                "SELECT id FROM " + BENE_TABLE + " WHERE account_id = " + accountId + " AND beneficiary_name = '" + name + "'");
        assertEquals("A rejected beneficiary must not be persisted", 0, rows.size());
    }

    // ============================ reusable helpers ============================
    private void assertResponseMatches(Response r, Map<String, String> e) {
        assertEquals("beneficiaryName", e.get("beneficiaryName"), r.jsonPath().getString("beneficiaryName"));
        assertEquals("relationship", e.get("relationship"), r.jsonPath().getString("relationship"));
        assertEquals("status", e.get("status"), r.jsonPath().getString("status"));
        assertEquals("percentage", Double.parseDouble(e.get("percentage")), r.jsonPath().getDouble("percentage"), 0.001);
    }

    private void assertDbMatches(Map<String, Object> db, Map<String, String> e) {
        assertEquals("beneficiary_name", e.get("beneficiaryName"), String.valueOf(db.get("beneficiary_name")));
        assertEquals("relationship", e.get("relationship"), String.valueOf(db.get("relationship")));
        assertEquals("status", e.get("status"), String.valueOf(db.get("status")));
        assertEquals("percentage", Double.parseDouble(e.get("percentage")),
                Double.parseDouble(String.valueOf(db.get("percentage"))), 0.001);
    }

    private void assertApiEqualsDb(Response r, Map<String, Object> db) {
        assertEquals("beneficiaryName", String.valueOf(db.get("beneficiary_name")), r.jsonPath().getString("beneficiaryName"));
        assertEquals("relationship", String.valueOf(db.get("relationship")), r.jsonPath().getString("relationship"));
        assertEquals("status", String.valueOf(db.get("status")), r.jsonPath().getString("status"));
        assertEquals("percentage", Double.parseDouble(String.valueOf(db.get("percentage"))),
                r.jsonPath().getDouble("percentage"), 0.001);
    }

    private Map<String, Object> fetchBeneRow(Long id) throws Exception {
        List<Map<String, Object>> rows = DBUtil.query(
                "SELECT beneficiary_name, relationship, date_of_birth, percentage, status "
                + "FROM " + BENE_TABLE + " WHERE id = " + id);
        assertEquals("Exactly one beneficiary row expected for id " + id, 1, rows.size());
        return rows.get(0);
    }

    private Long resolveAccountId(String accountNumber) throws Exception {
        List<Map<String, Object>> rows = DBUtil.query(
                "SELECT id FROM " + ACCOUNTS_TABLE + " WHERE account_number = '" + accountNumber + "'");
        assertEquals("Test account '" + accountNumber + "' must already exist", 1, rows.size());
        return ((Number) rows.get(0).get("id")).longValue();
    }

    private String beneJson(Map<String, String> d) {
        return "{"
                + "\"beneficiaryName\":\"" + d.get("beneficiaryName") + "\","
                + "\"relationship\":\"" + d.get("relationship") + "\","
                + "\"dateOfBirth\":\"" + d.get("dateOfBirth") + "\","
                + "\"percentage\":" + d.get("percentage") + ","
                + "\"status\":\"" + d.get("status") + "\"}";
    }
}
