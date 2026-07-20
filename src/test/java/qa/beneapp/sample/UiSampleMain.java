package qa.beneapp.sample;

import qa.beneapp.driver.PlaywrightDriver;
import qa.beneapp.pages.bene.LoginPage;
import qa.beneapp.pages.bene.SignupPage;
import qa.beneapp.utils.DBUtil;

/**
 * UiSampleMain - standalone UI demo across TWO pages (no Cucumber/JUnit).
 *
 * Flow (run main()):
 *   1. SignupPage : create a brand-new account (also auto-creates the 401k).
 *   2. LoginPage  : log in with the new credentials and confirm the dashboard.
 *   3. DB cleanup : delete the created account + user so the run leaves no trace.
 *
 * Requires the bene-ui dev server on the configured app.base.url (e.g. :4200).
 * Run from the IDE, or:
 *   mvn exec:java -Dexec.mainClass=qa.beneapp.sample.UiSampleMain -Dexec.classpathScope=test
 */
public class UiSampleMain {

    public static void main(String[] args) throws Exception {
        // ---- generate unique user data (username must match ^[a-z0-9_]+$) ----
        long ts = System.currentTimeMillis();
        String fullName = "Sample User";
        String username = "sampleuser" + (ts % 1000000);
        String email    = "sampleuser" + ts + "@email.com";
        String phone    = "+1-555-0123";
        String dob      = "1990-01-01";
        String ssn      = "4321";
        String balance  = "25000";
        String password = "Test@123";

        System.out.println("========== UI SAMPLE (Signup -> Login) ==========");
        System.out.println("New user: " + username + " / " + email);

        try {
            PlaywrightDriver.initDriver();

            // ---- PAGE 1: Sign up ----
            SignupPage signup = new SignupPage();
            signup.open();
            signup.signUp(fullName, username, email, phone, dob, ssn, balance, password);
            boolean signedUp = signup.isRedirectedToLogin();
            System.out.println("[1] Signup -> redirected to login: " + signedUp);

            // ---- PAGE 2: Log in ----
            LoginPage login = new LoginPage();
            login.login(username, password);
            boolean loggedIn = login.isLoggedIn();
            System.out.println("[2] Login -> dashboard shown: " + loggedIn
                    + " (user='" + (loggedIn ? login.getLoggedInUserName() : "n/a") + "')");

            System.out.println("========== RESULT: " + (signedUp && loggedIn ? "PASS" : "FAIL") + " ==========");
        } finally {
            PlaywrightDriver.quitDriver();

            // ---- STEP 3: DB cleanup (account in bene_schema, user in order_schema) ----
            int acct = DBUtil.update("DELETE FROM bene_schema.bene_accounts WHERE email = '" + email + "'");
            int usr  = DBUtil.update("DELETE FROM order_schema.users WHERE username = '" + username + "'");
            System.out.println("[3] DB cleanup -> accounts removed: " + acct + ", users removed: " + usr);
        }
    }
}
