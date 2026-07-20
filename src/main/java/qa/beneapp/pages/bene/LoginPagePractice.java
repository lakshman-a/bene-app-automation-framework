package qa.beneapp.pages.bene;

import qa.beneapp.config.ConfigManager;

public class LoginPagePractice extends BasePagePractice{

    private static final String USERNAME = "[data-testid='login-username']";
    private static final String PASSWORD = "[data-testid='login-password']";
    private static final String SUBMIT   = "[data-testid='login-submit']";
    private static final String ERROR    = "[data-testid='login-error']";
    private static final String USER_NAME_CHIP = "[data-testid='user-name']";
    private static final String LOGOUT   = "[data-testid='logout-button']";
    private static final String NAV_DASHBOARD = "[data-testid='nav-dashboard']";

   public void open(){
    navigateTo(ConfigManager.uiBaseUrl());
    waitForElement(SUBMIT);
   }

//    public boolean isOnLoginPage(){
//     return isVisible(SUBMIT);
//    }

   public void login(String username, String password){
    type(USERNAME, username);
    type(PASSWORD, password);   
    click(SUBMIT);
   }

//    public boolean isErrorDisplayed(){
//     return isVisible(ERROR);
//    }

   public void logout(){
    click(LOGOUT);
    waitForElement(SUBMIT);

   }



}
