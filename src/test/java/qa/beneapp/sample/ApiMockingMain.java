package qa.beneapp.sample;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * ============================================================================
 *  CONCEPT 2 of 3 : API MOCKING  (replace the response; backend NOT called)
 * ============================================================================
 *
 * SCENARIO - the classic interview question:
 *   "The Accounts API is NOT built yet. How do you still test / build the
 *    dashboard UI, and swap in the real API later?"
 *
 *   Given the accounts API does not exist yet (or returns data I can't control)
 *   And I MOCK the accounts calls with my own fake JSON
 *   When I log in and open my dashboard
 *   Then the dashboard renders the MOCKED account balance ($777,777.77)
 *
 * WHAT MOCKING IS:
 *   In the route callback we call route.fulfill(...) with OUR OWN response, so
 *   the real backend is never contacted for those endpoints.
 *
 * WHY A QA / DEV does this:
 *   - backend not ready  -> unblock UI work today, replace the mock later
 *   - reproduce hard states -> empty list, error 500, timeout, huge numbers
 *   - run UI tests offline / deterministically / independent of DB data
 *   - third-party APIs you cannot trigger on demand
 *
 * NOTE on tools: browser-level mocking like this is PLAYWRIGHT's job. RestAssured
 * is a client that CALLS real (or mock-server) APIs - it does not mock the
 * browser's traffic. For API-layer "not ready" you'd point RestAssured at a mock
 * server (e.g. WireMock) instead.
 *
 * Run:
 *   mvn exec:java -Dexec.mainClass=qa.beneapp.sample.ApiMockingMain -Dexec.classpathScope=test
 */
public class ApiMockingMain {

    private static final String UI_URL = "http://localhost:4200";

    // Our fake account. IMPORTANT: the email must match the logged-in user
    // (john.doe@email.com) so the dashboard keeps it after filtering by owner.
    private static final String MOCK_ACCOUNT = "{"
            + "\"id\":999001,"
            + "\"accountNumber\":\"401K-MOCK1\","
            + "\"accountHolderName\":\"John Doe (mock)\","
            + "\"email\":\"john.doe@email.com\","
            + "\"phone\":null,"
            + "\"dateOfBirth\":\"1985-04-12\","
            + "\"ssnLastFour\":\"1001\","
            + "\"totalBalance\":777777.77,"
            + "\"status\":\"ACTIVE\","
            + "\"beneficiaries\":[]"
            + "}";

    public static void main(String[] args) {
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(300));
        Page page = browser.newContext().newPage();
        page.setDefaultTimeout(30000);

        try {
            // STEP 1: MOCK the accounts LIST -> return one fake account.
            page.route("**/api/accounts", route -> route.fulfill(new Route.FulfillOptions()
                    .setStatus(200).setContentType("application/json").setBody("[" + MOCK_ACCOUNT + "]")));

            // STEP 2: MOCK the account DETAIL (the call that drives the visible
            //         balance on the dashboard). '*' matches the id segment.
            page.route("**/api/accounts/*/with-beneficiaries", route -> route.fulfill(new Route.FulfillOptions()
                    .setStatus(200).setContentType("application/json").setBody(MOCK_ACCOUNT)));

            // STEP 3: log in (the users API is real, so login works normally) and
            //         land on the dashboard, which now reads our mocked account.
            page.navigate(UI_URL + "/login");
            page.locator("[data-testid='login-username']").fill("johndoe");
            page.locator("[data-testid='login-password']").fill("demo123");
            page.locator("[data-testid='login-submit']").click();

            // STEP 4: the balance on screen comes from OUR mock, not the DB.
            Locator balance = page.locator("[data-testid='stat-balance']");
            balance.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            String shown = balance.innerText().trim();
            System.out.println("Balance shown on dashboard (from MOCK): " + shown);
            System.out.println("RESULT: UI rendered the mocked value = " + shown.contains("777,777"));

            // TO GO LIVE LATER: delete the two page.route(...) blocks (or call
            // page.unroute("**/api/accounts")) and the real Accounts API is used.
        } finally {
            browser.close();
            playwright.close();
        }
    }
}
