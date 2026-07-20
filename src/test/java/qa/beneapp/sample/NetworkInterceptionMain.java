package qa.beneapp.sample;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * ============================================================================
 *  CONCEPT 1 of 3 : NETWORK INTERCEPTION  (watch & verify real traffic)
 * ============================================================================
 *
 * SCENARIO (read it like a feature file):
 *   Given I am logging in to the bene-app
 *   When the dashboard loads and the UI calls GET /api/accounts
 *   Then that request MUST carry a valid "Authorization: Bearer <token>" header
 *   And the request still reaches the REAL backend (we only observe it)
 *
 * WHAT INTERCEPTION IS:
 *   We sit in the middle of the browser's network. We catch the request,
 *   INSPECT it (method, url, headers, body) and then call route.resume() to let
 *   the REAL request continue to the server. We do NOT change the response.
 *
 * WHY A QA DOES THIS (real use):
 *   - verify the frontend's API contract without trusting the backend
 *     (right endpoint, right auth header, right payload)
 *   - catch a missing/expired token, a wrong URL, or PII leaked in a request
 *   - confirm an action actually fires the expected call
 *
 * Run:
 *   mvn exec:java -Dexec.mainClass=qa.beneapp.sample.NetworkInterceptionMain -Dexec.classpathScope=test
 */
public class NetworkInterceptionMain {

    private static final String UI_URL = "http://localhost:4200";
    private static final String NAV_DASHBOARD = "[data-testid='nav-dashboard']";

    public static void main(String[] args) {
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(300));
        Page page = browser.newContext().newPage();
        page.setDefaultTimeout(30000);

        // We capture the header inside the route callback; a 1-element array lets
        // the lambda write to it.
        final String[] observedAuthHeader = { null };

        try {
            // STEP 1: INTERCEPT every call to the accounts API. Read the auth
            //         header, print the request, then RESUME so the REAL backend
            //         still answers (observe only - we change nothing).
            page.route("**/api/accounts", route -> {
                Request req = route.request();
                observedAuthHeader[0] = req.headers().get("authorization");
                System.out.println("[intercepted] " + req.method() + " " + req.url());
                route.resume();
            });

            // STEP 2: log in - this navigates to the dashboard, which fires GET /api/accounts.
            page.navigate(UI_URL + "/login");
            page.locator("[data-testid='login-username']").fill("johndoe");
            page.locator("[data-testid='login-password']").fill("demo123");
            page.locator("[data-testid='login-submit']").click();

            // STEP 3: wait until the dashboard is up so the request has happened.
            page.locator(NAV_DASHBOARD).waitFor(
                    new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

            // STEP 4: assert the contract we care about.
            System.out.println("Observed Authorization header: " + observedAuthHeader[0]);
            boolean ok = observedAuthHeader[0] != null && observedAuthHeader[0].startsWith("Bearer ");
            System.out.println("RESULT: accounts request carried a Bearer token = " + ok);
        } finally {
            browser.close();
            playwright.close();
        }
    }
}
