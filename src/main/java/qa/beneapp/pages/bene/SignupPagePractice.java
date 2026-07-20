package qa.beneapp.pages.bene;

import qa.beneapp.config.ConfigManager;
import qa.beneapp.pages.BasePage;

public class SignupPagePractice extends BasePage {



    private static final String FULLNAME = "#fullName";
    private static final String USERNAME = "#username";
    private static final String EMAIL = "#email";
    private static final String PHONE = "#phone";
    private static final String DOB = "#dob";
    private static final String OPENING_BALANCE  = "#opening";
    private static final String PASSWORD = "#password";
    private static final String CREATEACCOUNT_BTN = "#signup-submit";
    private static final String SSN_LAST_FOUR = "#ssn";
      private static final String ERROR    = "[data-testid='signup-error']";

    public void open(){
        navigateTo(ConfigManager.uiBaseUrl() + "/signup");
        waitForElement(CREATEACCOUNT_BTN);

        
    }

    public void signUp(String fullName, String userName, String email, String phoneNum, String dob, String SSNLastFour, String openingBalance, String Password){
        log.info("Signing up new user '{}' ({}) " , userName, email);
        type(FULLNAME, fullName);
        type(USERNAME, userName);
        type(EMAIL, email);
        type(PHONE, phoneNum);
        type(DOB, dob);
        type(SSN_LAST_FOUR, SSNLastFour);
        type(OPENING_BALANCE, openingBalance);
        type(PASSWORD, Password);
        click(CREATEACCOUNT_BTN);


    }

    public boolean isErrorDisplayed(){
        return isVisible(ERROR);
    }


}
