package qa.beneapp.sample;

import java.util.List;
import java.util.Map;

import io.restassured.response.Response;
import qa.beneapp.utils.ApiUtil;
import qa.beneapp.utils.DBUtil;

/**
 * ApiSampleMain - standalone end-to-end API + DB demo (no Cucumber/JUnit).
 *
 * Flow (run main()):
 *   1. DB cleanup    : delete the account row first so the test always re-runs.
 *   2. API POST      : create the account via the REST API.
 *   3. Print response: show the raw API response.
 *   4. DB read-back  : query bene_accounts and get the row as List&lt;Map&gt;.
 *   5. Validate      : the values we submitted == the values stored in the DB.
 *   6. DB cleanup    : delete the account again to leave a clean state.
 *
 * Run from the IDE, or:
 *   mvn exec:java -Dexec.mainClass=qa.beneapp.sample.ApiSampleMain -Dexec.classpathScope=test
 */
public class ApiSampleMain {

    public static void main(String[] args) throws Exception {
        // ---- generate unique account data so re-runs never clash ----
        long ts = System.currentTimeMillis();
        String accountNumber = "401K-" + (ts % 1000000);   // 6 digits, matches 401K-[0-9]{5,10}
        String holder        = "Sample Holder";
        String email         = "sample_" + ts + "@email.com";
        String dob           = "1990-01-01";
        String ssn           = "4321";
        double balance       = 50000.00;
        String status        = "ACTIVE";

        String table = "bene_schema.bene_accounts";
        System.out.println("========== API + DB SAMPLE ==========");
        System.out.println("Account number under test: " + accountNumber);

        // ---- STEP 1: delete first (precondition cleanup) ----
        int deleted = DBUtil.update("DELETE FROM " + table + " WHERE account_number = '" + accountNumber + "'");
        System.out.println("[1] DB precondition delete -> rows removed: " + deleted);

        // ---- STEP 2: POST create via API ----
        String body = "{"
                + "\"accountNumber\":\"" + accountNumber + "\","
                + "\"accountHolderName\":\"" + holder + "\","
                + "\"email\":\"" + email + "\","
                + "\"dateOfBirth\":\"" + dob + "\","
                + "\"ssnLastFour\":\"" + ssn + "\","
                + "\"totalBalance\":" + balance + ","
                + "\"status\":\"" + status + "\"}";
        Response response = ApiUtil.post("/api/accounts", body);

        // ---- STEP 3: print the response ----
        System.out.println("[2] API POST /api/accounts -> status: " + response.getStatusCode());
        System.out.println("[3] API response body:\n" + response.getBody().asPrettyString());

        // ---- STEP 4: read the row back from the DB ----
        List<Map<String, Object>> rows = DBUtil.query(
                "SELECT account_number, account_holder_name, email, total_balance, status "
                + "FROM " + table + " WHERE account_number = '" + accountNumber + "'");
        System.out.println("[4] DB read-back (List<Map>): " + rows);

        // ---- STEP 5: validate API-submitted == DB-stored ----
        boolean pass = false;
        if (rows.size() == 1) {
            Map<String, Object> row = rows.get(0);
            boolean numberOk  = accountNumber.equals(String.valueOf(row.get("account_number")));
            boolean holderOk  = holder.equals(String.valueOf(row.get("account_holder_name")));
            boolean emailOk   = email.equals(String.valueOf(row.get("email")));
            boolean balanceOk = Double.compare(balance, Double.parseDouble(String.valueOf(row.get("total_balance")))) == 0;
            boolean statusOk  = status.equals(String.valueOf(row.get("status")));
            pass = numberOk && holderOk && emailOk && balanceOk && statusOk;

            System.out.println("[5] Validation:");
            System.out.println("      account_number : " + numberOk);
            System.out.println("      holder_name    : " + holderOk);
            System.out.println("      email          : " + emailOk);
            System.out.println("      total_balance  : " + balanceOk);
            System.out.println("      status         : " + statusOk);
        } else {
            System.out.println("[5] Validation FAILED - expected exactly 1 DB row, found " + rows.size());
        }

        // ---- STEP 6: cleanup ----
        int cleaned = DBUtil.update("DELETE FROM " + table + " WHERE account_number = '" + accountNumber + "'");
        System.out.println("[6] DB cleanup delete -> rows removed: " + cleaned);

        System.out.println("========== RESULT: " + (pass ? "PASS" : "FAIL") + " ==========");
    }
}
