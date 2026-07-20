package qa.beneapp.sample;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.WaitForSelectorState;

public class APIMOCKING {

    private static final String UI_URL = "http://localhost:4200";
    private static final String MOCK_ACCOUNT = "{"
            + "\"id\":999001,"
            + "\"accountNumber\":\"401K-MOCK1\","
            + "\"accountHolderName\":\"John Doe (mock)\","
            + "\"email\":\"john.doe@email.com\","
            + "\"phone\":null,"
            + "\"dateOfBirth\":\"1985-04-12\","
            + "\"ssnLastFour\":\"1001\","
            + "\"totalBalance\":777777.77,"
            + "\"status\":\"ACTIVE\","
            + "\"beneficiaries\":[]"
            + "}";

            public static void main(String[] args) {
                
                 Playwright playwright = Playwright.create();
                Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(300));
                Page page = browser.newContext().newPage();
                page.setDefaultTimeout(3000);

            page.route("**api/accounts" , route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(200).setContentType("application/json").setBody(MOCK_ACCOUNT)));
                    

            page.navigate(UI_URL);
            page.locator("[data-testid='login-username']").fill("johndoe");
            page.locator("[data-testid='login-password']").fill("12345");
            page.locator("#submit").click();

            Locator balance = page.locator("#balance");
            balance.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));  
            
            
            final String[] observedAuthHeader = {null};

            page.route("**api/accounts", route -> {
                Request req = route.request();
                observedAuthHeader[0] = req.headers().get("application/json");
                System.out.println("[interception]" + req.method() + " " + req.url());
                route.resume();
            });






            }

}
