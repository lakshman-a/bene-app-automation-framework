package qa.beneapp.sample;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * UiSamplePlainMain - the SAME signup -> login -> cleanup flow as UiSampleMain,
 * but written with RAW Playwright objects and inline element actions in this one
 * class. It does NOT use PlaywrightDriver, BasePage, or any Page Object, and it
 * does its DB cleanup with inline JDBC. Everything is visible in a single file
 * so the end-to-end mechanics are easy to follow.
 *
 * Requires the bene-ui dev server (:4200) and PostgreSQL to be running.
 * Run from the IDE, or:
 *   mvn exec:java -Dexec.mainClass=qa.beneapp.sample.UiSamplePlainMain -Dexec.classpathScope=test
 */
public class UiSamplePlainMain {

    // ---- self-contained config (no ConfigManager) ----
    private static final String UI_URL  = "http://localhost:4200";
    private static final String DB_URL  = "jdbc:postgresql://localhost:5432/postgres";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "123456";

    public static void main(String[] args) throws Exception {
        long ts = System.currentTimeMillis();
        String fullName = "Plain User";
        String username = "plainuser" + (ts % 1000000);
        String email    = "plainuser" + ts + "@email.com";
        String phone    = "+1-555-0123";
        String dob      = "1990-01-01";
        String ssn      = "4321";
        String balance  = "25000";
        String password = "Test@123";

        System.out.println("========== UI SAMPLE (raw Playwright) ==========");
        System.out.println("New user: " + username + " / " + email);

        // ---- create Playwright objects directly ----
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(300));
        BrowserContext context = browser.newContext(
                new Browser.NewContextOptions().setViewportSize(1920, 1080));
        Page page = context.newPage();
        page.setDefaultTimeout(30000);

        boolean signedUp = false;
        boolean loggedIn = false;
        try {
            // ===== PAGE 1: Sign up =====
            page.navigate(UI_URL + "/signup");
            page.locator("[data-testid='signup-fullname']").fill(fullName);
            page.locator("[data-testid='signup-username']").fill(username);
            page.locator("[data-testid='signup-email']").fill(email);
            page.locator("[data-testid='signup-phone']").fill(phone);
            page.locator("[data-testid='signup-dob']").fill(dob);
            page.locator("[data-testid='signup-ssn']").fill(ssn);
            page.locator("[data-testid='signup-balance']").fill(balance);
            page.locator("[data-testid='signup-password']").fill(password);
            page.locator("[data-testid='signup-submit']").click();

            // success = redirected to the login page
            Locator loginSubmit = page.locator("[data-testid='login-submit']");
            loginSubmit.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            signedUp = loginSubmit.isVisible();
            System.out.println("[1] Signup -> redirected to login: " + signedUp);

            // ===== PAGE 2: Log in =====
            page.locator("[data-testid='login-username']").fill(username);
            page.locator("[data-testid='login-password']").fill(password);
            loginSubmit.click();

            Locator navDashboard = page.locator("[data-testid='nav-dashboard']");
            navDashboard.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            loggedIn = navDashboard.isVisible() && page.locator("[data-testid='user-name']").isVisible();
            String who = loggedIn ? page.locator("[data-testid='user-name']").innerText() : "n/a";
            System.out.println("[2] Login -> dashboard shown: " + loggedIn + " (user='" + who + "')");

            System.out.println("========== RESULT: " + (signedUp && loggedIn ? "PASS" : "FAIL") + " ==========");
        } finally {
            // ---- close Playwright ----
            context.close();
            browser.close();
            playwright.close();

            // ---- inline JDBC cleanup (account in bene_schema, user in order_schema) ----
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 Statement stmt = conn.createStatement()) {
                int acct = stmt.executeUpdate(
                        "DELETE FROM bene_schema.bene_accounts WHERE email = '" + email + "'");
                int usr = stmt.executeUpdate(
                        "DELETE FROM order_schema.users WHERE username = '" + username + "'");
                System.out.println("[3] DB cleanup -> accounts removed: " + acct + ", users removed: " + usr);
            }
        }
    }
}
