package qa.beneapp.sample;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Request;

public class NetworkInterceptionPractise {

    public static void main(String[] args) {
            Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(300));
        Page page = browser.newContext().newPage();
        page.setDefaultTimeout(30000);

      final String[] observedAuthHeader ={null};

            page.route("**api/accounts", route -> {
                Request req = route.request();
                observedAuthHeader[0] = req.headers().get("application/json");
                System.out.println("[Intercepted]" + req.method() + req.url());
                route.resume();

            });






}
}
