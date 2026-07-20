package qa.beneapp.pages.bene;

import qa.beneapp.config.ConfigManager;
import qa.beneapp.pages.BasePage;

/**
 * SignupPage - Page Object for the bene-app end-user sign-up flow.
 *
 * Submitting the form creates the user AND auto-creates their first 401k
 * account (linked by email), then redirects to /login.
 */
public class SignupPage extends BasePage {

    private static final String FULLNAME = "[data-testid='signup-fullname']";
    private static final String USERNAME = "[data-testid='signup-username']";
    private static final String EMAIL    = "[data-testid='signup-email']";
    private static final String PHONE    = "[data-testid='signup-phone']";
    private static final String DOB      = "[data-testid='signup-dob']";
    private static final String SSN      = "[data-testid='signup-ssn']";
    private static final String BALANCE  = "[data-testid='signup-balance']";
    private static final String PASSWORD = "[data-testid='signup-password']";
    private static final String SUBMIT   = "[data-testid='signup-submit']";
    private static final String ERROR    = "[data-testid='signup-error']";
    private static final String LOGIN_SUBMIT = "[data-testid='login-submit']";

    /** Open the sign-up page (UI base URL + /signup). */
    public void open() {
        navigateTo(ConfigManager.uiBaseUrl() + "/signup");
        waitForElement(SUBMIT);
    }

    /** Fill the whole sign-up form and submit. */
    public void signUp(String fullName, String username, String email, String phone,
                       String dob, String ssnLast4, String openingBalance, String password) {
        log.info("Signing up new user '{}' ({})", username, email);
        type(FULLNAME, fullName);
        type(USERNAME, username);
        type(EMAIL, email);
        type(PHONE, phone);
        type(DOB, dob);
        type(SSN, ssnLast4);
        type(BALANCE, openingBalance);
        type(PASSWORD, password);
        click(SUBMIT);
    }

    public boolean isErrorDisplayed() {
        return isVisible(ERROR);
    }

    /** Waits for the signup error to render (it appears after the async call). */
    public String getErrorMessage() {
        waitForElement(ERROR);
        return getText(ERROR);
    }

    /** Sign-up succeeds by redirecting to the login page. */
    public boolean isRedirectedToLogin() {
        waitForElement(LOGIN_SUBMIT);
        return isVisible(LOGIN_SUBMIT);
    }
}
