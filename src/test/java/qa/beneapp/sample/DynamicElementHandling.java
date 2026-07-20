package qa.beneapp.sample;

import java.util.List;
import java.util.Map;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;


public class DynamicElementHandling {

    // private static final String UI_URL = "http://localhost:4200";
    // private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    // private static final String DB_USER = "postgres";
    // private static final String DB_PASS = "123456";
    // private static final String ACCOUNT_NUMBER = "401K-30001"; // johndoe's account

    // public static void main(String[] args) {
    //     seedBeneficiaries();

    //     Playwright playwright = Playwright.create();
    //     Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(100));
    //     Page page = browser.newContext().newPage();
    //     page.setDefaultTimeout(3000);

    //     try {
    //          page.navigate(UI_URL + "/login");
    //         page.locator("[data-testid='login-username']").fill("johndoe");
    //         page.locator("[data-testid='login-password']").fill("demo123");
    //         page.locator("[data-testid='login-submit']").click();

    //         page.locator("[data-testid='account-stats']").waitFor(new Locator.WaitForOptions()
    //         .setState(WaitForSelectorState.VISIBLE));
    //         page.locator("[data-testid='nav-beneficiaries']").click();

    //         page.locator("//table[@id='beneficiaries-table']//tr[@data-bene-name]").waitFor
    //         (new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

    //          List<Map<String, String>> rows = extractTable(page);
    //          for(int i =0; i<rows.size(); i++){
    //             System.out.println("rows" + (i+1)+ ":" + rows.get(i));

    //          }




            
    //     } finally {
    //     }

    //     private static List<Map<String, String>> extractTable(Page page) {

    //   Locator table = page.locator("//table[@id='beneficiaries-table']");
    //   Locator rows = table.locator("tr");
    //   int rowCount = rows.count();
    //   System.out.println("Rows count is " + rowCount);

    //   for(int i =0; i<rowCount; i++){
    //     Locator cells = rows.nth(i).locator("td");
    //     int cellCount = cells.count();
    //     System.out.println("Column count is " + cellCount);

    //     for(int j =0; j<cellCount; j++){
    //         String cellData = cells.nth(i).textContent();
    //         System.out.println("i = "+ i + " j " + j + cellData);
    //     }
    //   }




    //     }

        



        
    // }

    // private static void seedBeneficiaries() {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'seedBeneficiaries'");
    // }


}
