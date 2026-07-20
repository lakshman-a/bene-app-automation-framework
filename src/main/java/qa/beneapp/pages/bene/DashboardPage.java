package qa.beneapp.pages.bene;

import qa.beneapp.pages.BasePage;

/**
 * DashboardPage - the end-user "My 401k" home shown after a successful login.
 *
 * Used both as the post-login landing check and as the launch point to the
 * Beneficiaries / Transactions / Withdrawals pages via the top navigation.
 */
public class DashboardPage extends BasePage {

    private static final String TITLE        = "[data-testid='dashboard-title']";
    private static final String ACCOUNT_STATS = "[data-testid='account-stats']";
    private static final String ACCOUNT_NUMBER = "[data-testid='detail-number']";
    private static final String NAV_BENEFICIARIES = "[data-testid='nav-beneficiaries']";

    /** True once the dashboard home has rendered (login landed successfully). */
    public boolean isLoaded() {
        waitForElement(TITLE);
        return isVisible(TITLE);
    }

    /** Wait until an account is selected and its details are on screen. */
    public void waitForMyAccount() {
        waitForElement(ACCOUNT_STATS);
        waitForElement(ACCOUNT_NUMBER);
    }

    public String getActiveAccountNumber() {
        return getText(ACCOUNT_NUMBER).trim();
    }

    public void openBeneficiaries() {
        click(NAV_BENEFICIARIES);
    }
}
