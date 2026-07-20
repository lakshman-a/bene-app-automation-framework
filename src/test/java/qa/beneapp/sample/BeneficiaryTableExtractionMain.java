package qa.beneapp.sample;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * BeneficiaryTableExtractionMain - a plain, self-contained demo (no framework
 * classes) that reads a DYNAMIC HTML table and returns it as List&lt;Map&gt;.
 *
 * Flow:
 * 1. ARRANGE (JDBC): make sure account 401K-30001 (johndoe) has a few
 * beneficiaries, so the table actually has rows to read.
 * 2. ACT (Playwright): log in, open the Beneficiaries page.
 * 3. EXTRACT: walk the table using the raw table/thead/th/tbody/tr/td tags,
 * print each row, and collect everything into a
 * List&lt;Map&lt;String,String&gt;&gt;
 * keyed by the column header.
 *
 * Run:
 * mvn exec:java
 * -Dexec.mainClass=qa.beneapp.sample.BeneficiaryTableExtractionMain
 * -Dexec.classpathScope=test
 */
public class BeneficiaryTableExtractionMain {

    private static final String UI_URL = "http://localhost:4200";
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "123456";
    private static final String ACCOUNT_NUMBER = "401K-30001"; // johndoe's account

    public static void main(String[] args) throws Exception {
        // ---------- 1) ARRANGE: seed a few beneficiaries via JDBC ----------
        seedBeneficiaries();

        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(300));
        Page page = browser.newContext().newPage();
        page.setDefaultTimeout(30000);

        try {
            // ---------- 2) ACT: log in and open the Beneficiaries page ----------
            page.navigate(UI_URL + "/login");
            page.locator("[data-testid='login-username']").fill("johndoe");
            page.locator("[data-testid='login-password']").fill("demo123");
            page.locator("[data-testid='login-submit']").click();

            // Login lands on the dashboard; wait until the account is selected
            // (that is what makes the Beneficiaries page show data), then open it.
            page.locator("[data-testid='account-stats']").waitFor(
                    new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            page.locator("[data-testid='nav-beneficiaries']").click();

            // Wait for the real beneficiary rows to render (they carry data-bene-name).
            page.locator("xpath=//table[@id='beneficiaries-table']//tr[@data-bene-name]").first().waitFor(
                    new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

            // ---------- 3) EXTRACT the dynamic table ----------
            List<Map<String, String>> rows = extractTable(page);

            // ---------- print the collected List<Map> ----------
            System.out.println("\n========== Beneficiaries as List<Map<String,String>> ==========");
            for (int i = 0; i < rows.size(); i++) {
                System.out.println("Row " + (i + 1) + ": " + rows.get(i));
            }
            System.out.println("Total rows extracted: " + rows.size());
        } finally {
            browser.close();
            playwright.close();
        }
    }

    /**
     * Reads any table using XPath locators. Steps:
     * 1. get the table,
     * 2. read the header cells (th) with their count,
     * 3. get the data rows (tr that contain td) with their count,
     * 4. two loops - outer over each tr, inner over each td - collecting the
     * values into one Map per row and printing every cell.
     */
    private static List<Map<String, String>> extractTable(Page page) {
        // 1) Get the table first.
        // -- //table[@id='beneficiaries-table']
        Locator table = page.locator("//table[@id='beneficiaries-table']");



        // 2) Header cells (th) + count.
        // Locator headerCells = table.locator("xpath=.//th");
        // int headerCount = headerCells.count();
        // List<String> headers = new ArrayList<>();
        // for (int h = 0; h < headerCount; h++) {
        // headers.add(headerCells.nth(h).innerText().trim());
        // }
        // System.out.println("Header count: " + headerCount + " -> " + headers);

        // 3) Data rows = tr that actually contain td cells (this skips the header
        // row and any empty-state row), plus their count.
        Locator rows = table.locator("tr");
        int rowCount = rows.count();
        System.out.println("Row count: " + rowCount);

        // 4) Two loops: outer over rows (tr), inner over cells (td).
        List<Map<String, String>> data = new ArrayList<>();
       
        // Rows loop
        for (int i = 0; i < rowCount; i++) {

             Locator cells = rows.nth(i).locator("td");
            int cellCount = cells.count();

            for (int j = 0; j < cellCount; j++) {
                String cellData = cells.nth(j).textContent();
                System.out.println("i = " + i + " j = " + j + " " + cellData);
            }

            // Map<String, String> row = new LinkedHashMap<>();
            // for (int c = 0; c < cellCount; c++) {
            // String value = cells.nth(c).innerText().trim();
            // String key = c < headers.size() ? headers.get(c) : ("column" + (c + 1));
            // row.put(key, value);
            // System.out.println(" row " + (r + 1) + ", cell " + (c + 1) + " [" + key + "]
            // = " + value);
            // }
            // data.add(row);
        }
        return data;
    }

    /**
     * Ensure the account has 3 beneficiaries (clear then insert) so the table has
     * data.
     */
    private static void seedBeneficiaries() throws Exception {
        Class.forName("org.postgresql.Driver");
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                Statement stmt = conn.createStatement()) {

            long accountId;
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT id FROM bene_schema.bene_accounts WHERE account_number = '" + ACCOUNT_NUMBER + "'")) {
                if (!rs.next()) {
                    throw new IllegalStateException("Account " + ACCOUNT_NUMBER + " not found - create it first.");
                }
                accountId = rs.getLong("id");
            }

            stmt.executeUpdate("DELETE FROM bene_schema.beneficiaries WHERE account_id = " + accountId);
            String[][] people = {
                    { "Jane Smith", "SPOUSE", "1990-05-01", "40" },
                    { "Michael Doe", "CHILD", "2010-08-12", "35" },
                    { "Emma Doe", "CHILD", "2012-03-04", "25" },
            };
            for (String[] p : people) {
                stmt.executeUpdate(
                        "INSERT INTO bene_schema.beneficiaries "
                                + "(account_id, beneficiary_name, relationship, date_of_birth, percentage, status) "
                                + "VALUES (" + accountId + ", '" + p[0] + "', '" + p[1] + "', '" + p[2] + "', " + p[3]
                                + ", 'ACTIVE')");
            }
            System.out.println("Seeded " + people.length + " beneficiaries for account " + ACCOUNT_NUMBER + " (id="
                    + accountId + ")");
        }
    }
}
