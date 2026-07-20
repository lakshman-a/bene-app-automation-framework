package qa.beneapp.pages.bene;

import qa.beneapp.config.ConfigManager;
import qa.beneapp.pages.BasePage;

/**
 * LoginPage - Page Object for the bene-app end-user login / logout flow.
 *
 * Selectors use the app's stable data-testid hooks (see bene-ui components).
 */
public class LoginPage extends BasePage {

    private static final String USERNAME = "[data-testid='login-username']";
    private static final String PASSWORD = "[data-testid='login-password']";
    private static final String SUBMIT   = "[data-testid='login-submit']";
    private static final String ERROR    = "[data-testid='login-error']";
    private static final String USER_NAME_CHIP = "[data-testid='user-name']";
    private static final String LOGOUT   = "[data-testid='logout-button']";
    private static final String NAV_DASHBOARD = "[data-testid='nav-dashboard']";

    /** Open the login page (UI base URL + /login). */
    public void open() {
        navigateTo(ConfigManager.uiBaseUrl() + "/login");
        waitForElement(SUBMIT);
    }

    public boolean isOnLoginPage() {
        return isVisible(SUBMIT);
    }

    /** Fill credentials and submit. */
    public void login(String username, String password) {
        log.info("Logging in as '{}'", username);
        type(USERNAME, username);
        type(PASSWORD, password);
        click(SUBMIT);
    }

    /** True once the authenticated end-user dashboard is reached. */
    public boolean isLoggedIn() {
        waitForElement(NAV_DASHBOARD);
        return isVisible(NAV_DASHBOARD) && isVisible(USER_NAME_CHIP);
    }

    public String getLoggedInUserName() {
        return getText(USER_NAME_CHIP);
    }

    public boolean isErrorDisplayed() {
        return isVisible(ERROR);
    }

    /** Waits for the login error to render (it appears after the async login call). */
    public String getErrorMessage() {
        waitForElement(ERROR);
        return getText(ERROR);
    }

    /** Log out from the top navigation; returns to the login page. */
    public void logout() {
        log.info("Logging out");
        click(LOGOUT);
        waitForElement(SUBMIT);
    }
}
