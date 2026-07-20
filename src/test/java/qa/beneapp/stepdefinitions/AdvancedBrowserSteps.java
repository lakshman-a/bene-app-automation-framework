package qa.beneapp.stepdefinitions;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.WaitForSelectorState;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import qa.beneapp.driver.PlaywrightDriver;

/**
 * Step definitions for AdvancedBrowserUI.feature - network interception,
 * API mocking and cookie/storage-state checks. These steps work directly with
 * the Playwright Page (no page object) because they exercise the browser/network
 * layer rather than page elements; each is commented to explain the technique.
 *
 * Login is reused from LoginUISteps; everything here shares the same Page because
 * PlaywrightDriver holds a single page for the scenario.
 */
public class AdvancedBrowserSteps {

    private static final String ACCOUNTS_API = "**/api/accounts";
    private static final String NAV_DASHBOARD = "[data-testid='nav-dashboard']";
    private static final String EMPTY_STATE = "[data-testid='no-account-selected']";

    private String observedAuthHeader;

    private Page page() {
        return PlaywrightDriver.getPage();
    }

    // ---------- 1) Network interception (observe only) ----------
    @And("I start observing requests to the accounts API")
    public void iStartObservingAccountsRequests() {
        // page.onRequest fires for every outgoing request - we just read the
        // headers of the accounts call. We do NOT block or change it.
        page().onRequest(req -> {
            if (req.url().contains("/api/accounts") && observedAuthHeader == null) {
                observedAuthHeader = req.headers().get("authorization");
            }
        });
    }

    @Then("the observed accounts request should include a bearer token")
    public void theObservedRequestShouldIncludeBearerToken() {
        // The accounts call fires when the dashboard loads after login.
        waitForVisible(NAV_DASHBOARD);
        for (int i = 0; i < 25 && observedAuthHeader == null; i++) {
            page().waitForTimeout(200);
        }
        assertNotNull("The UI should have called the accounts API", observedAuthHeader);
        assertTrue("Accounts request must carry a Bearer token, was: " + observedAuthHeader,
                observedAuthHeader.startsWith("Bearer "));
    }

    // ---------- 2) API mocking (fulfill with our own response) ----------
    @And("the accounts API is mocked to return no accounts")
    public void theAccountsApiIsMockedToReturnNoAccounts() {
        // Intercept the accounts call and answer it ourselves with an empty
        // array. The real backend is never reached for this endpoint.
        page().route(ACCOUNTS_API, route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/json")
                .setBody("[]")));
    }

    @Then("my dashboard should show the no-accounts empty state")
    public void myDashboardShouldShowEmptyState() {
        // With the mocked empty list the dashboard renders its empty state.
        waitForVisible(EMPTY_STATE);
        assertTrue("Empty state should be shown for a user with no accounts",
                page().locator(EMPTY_STATE).isVisible());
    }

    // ---------- 3) Cookies & storage state ----------
    @Then("a session cookie {string} should be present")
    public void aSessionCookieShouldBePresent(String cookieName) {
        waitForVisible(NAV_DASHBOARD);   // ensure login completed
        List<Cookie> cookies = page().context().cookies();
        boolean found = cookies.stream()
                .anyMatch(c -> c.name.equals(cookieName) && c.value != null && !c.value.isEmpty());
        assertTrue("Session cookie '" + cookieName + "' should be set after login", found);
    }

    @And("local storage should contain the logged-in user {string}")
    public void localStorageShouldContainUser(String username) {
        Object stored = page().evaluate("() => localStorage.getItem('bene_user')");
        assertNotNull("localStorage 'bene_user' should be set", stored);
        assertTrue("localStorage should reference the logged-in user '" + username + "'",
                String.valueOf(stored).contains(username));
    }

    @And("my session should survive a page reload")
    public void mySessionShouldSurviveAReload() {
        // Reload from scratch: if the session truly lives in cookie/localStorage,
        // the app restores it and we stay on an authenticated page (no redirect
        // back to login).
        page().reload();
        waitForVisible(NAV_DASHBOARD);
        assertTrue("Should remain logged in after reload", page().locator(NAV_DASHBOARD).isVisible());
    }

    private void waitForVisible(String selector) {
        page().locator(selector).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }
}
