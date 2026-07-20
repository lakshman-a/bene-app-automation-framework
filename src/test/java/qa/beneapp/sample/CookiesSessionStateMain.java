package qa.beneapp.sample;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * ============================================================================
 *  CONCEPT 3 of 3 : COOKIES & STORAGE STATE  (session persistence + reuse)
 * ============================================================================
 *
 * SCENARIO:
 *   Given I log in ONCE in a fresh browser context (A)
 *   And the app stores my session in a cookie (bene_session) and in
 *       localStorage (bene_user / bene_session / bene_role)
 *   When I SAVE that "storage state" and open a BRAND-NEW context (B) preloaded
 *       with it, then go straight to /dashboard
 *   Then context B is ALREADY logged in - no login UI required
 *
 * COOKIE vs SESSION (plain english):
 *   - a COOKIE is a small key/value the browser stores and sends back to the
 *     server automatically (here: bene_session).
 *   - the SESSION is the "I am logged in" state. This app keeps it client-side
 *     in localStorage (+ a cookie).
 *   - "STORAGE STATE" = cookies + localStorage captured together as one JSON.
 *
 * WHY / WHEN a QA uses this (and the atomicity question):
 *   Tests stay ATOMIC - each test still runs in its OWN fresh context. But
 *   instead of every test clicking through the login UI, we log in ONCE, save
 *   the storage state to a file, and INJECT it into each context. It's not
 *   "one-time testing" - it's a per-suite setup reused by many independent
 *   tests to skip the slow login step. Use it when login isn't what you're
 *   testing; write dedicated login tests separately.
 *
 * Run:
 *   mvn exec:java -Dexec.mainClass=qa.beneapp.sample.CookiesSessionStateMain -Dexec.classpathScope=test
 */
public class CookiesSessionStateMain {

    private static final String UI_URL = "http://localhost:4200";
    private static final String NAV_DASHBOARD = "[data-testid='nav-dashboard']";
    private static final Path STATE_FILE = Paths.get("target/auth-state.json");

    public static void main(String[] args) {
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(300));
        try {
            // ================= Context A: real login, then SAVE state =================
            BrowserContext ctxA = browser.newContext();
            Page pageA = ctxA.newPage();
            pageA.setDefaultTimeout(30000);

            pageA.navigate(UI_URL + "/login");
            pageA.locator("[data-testid='login-username']").fill("johndoe");
            pageA.locator("[data-testid='login-password']").fill("demo123");
            pageA.locator("[data-testid='login-submit']").click();
            pageA.locator(NAV_DASHBOARD).waitFor(
                    new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

            // Inspect the COOKIE the server/app set.
            for (Cookie c : ctxA.cookies()) {
                if (c.name.equals("bene_session")) {
                    System.out.println("Cookie  bene_session = " + c.value);
                }
            }
            // Inspect the SESSION kept in localStorage.
            Object user = pageA.evaluate("() => localStorage.getItem('bene_user')");
            System.out.println("Storage bene_user     = " + user);

            // SAVE cookies + localStorage together as the "storage state".
            ctxA.storageState(new BrowserContext.StorageStateOptions().setPath(STATE_FILE));
            System.out.println("Saved storage state -> " + STATE_FILE.toAbsolutePath());
            ctxA.close();

            // ============ Context B: REUSE the state, skip the login UI ============
            BrowserContext ctxB = browser.newContext(
                    new Browser.NewContextOptions().setStorageStatePath(STATE_FILE));
            Page pageB = ctxB.newPage();
            pageB.setDefaultTimeout(30000);

            // Go STRAIGHT to a protected page - no username/password typed.
            pageB.navigate(UI_URL + "/dashboard");

            boolean loggedIn;
            try {
                pageB.locator(NAV_DASHBOARD).waitFor(
                        new Locator.WaitForOptions().setTimeout(8000).setState(WaitForSelectorState.VISIBLE));
                loggedIn = true;
            } catch (Exception e) {
                loggedIn = false;   // if the state wasn't restored we'd be bounced to /login
            }
            System.out.println("Context B reached the dashboard WITHOUT logging in = " + loggedIn);
            System.out.println("RESULT: storage-state reuse skipped the login UI = " + loggedIn);
            ctxB.close();
        } finally {
            browser.close();
            playwright.close();
        }
    }
}
