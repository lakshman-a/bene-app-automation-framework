package qa.beneapp.pages.bene;

import qa.beneapp.pages.BasePage;

public class DashboardPagePractics extends BasePage {

    private static final String TITLE = "//h1[text()='My 401k']";
    private static final String ACCOUNT_STATUS = "//div[text()='Status']";
    private static final String ACCOUNT_NUMBER = "[data-testid='detail-number']";
    private static final String NAV_BENEFICIARIES = "[data-testid='nav-beneficiaries']";


    public boolean isLoaded(){
        waitForElement(TITLE);
        return isVisible(TITLE);
    }

    public void waitForMyAccount(){
        waitForElement(ACCOUNT_STATUS);
        waitForElement(ACCOUNT_NUMBER);
    }

    public String getActiveAccountNumber(){
        return getText(ACCOUNT_NUMBER).trim();
    }

    public void openBeneficiaries(){
        click(NAV_BENEFICIARIES);
    }






}
