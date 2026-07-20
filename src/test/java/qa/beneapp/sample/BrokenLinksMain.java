package qa.beneapp.sample;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

/**
 * ============================================================================
 * INTERVIEW PROGRAM : Find BROKEN LINKS on a web page (Playwright)
 * ============================================================================
 *
 * QUESTION: "How do you find broken links on a page with Playwright?"
 *
 * IDEA (3 lines to say in an interview):
 * 1. Open the page (here: Google, run a sample search) and collect all links
 * -> page.locator("a")
 * 2. For each link, read its href and send an HTTP request
 * -> page.request().get(url)
 * 3. A link is BROKEN if the status is >= 400 (or the request throws)
 *
 * Everything is inside main() with no helper methods, so it is easy to write
 * on a whiteboard.
 *
 * NOTE: Google actively blocks automation, so from a bot/headless browser it
 * may
 * return a stripped page (few links). The LOGIC below is the point; to see a
 * rich
 * result just change the navigate() URL to any page you are allowed to
 * automate.
 *
 * Run:
 * mvn exec:java -Dexec.mainClass=qa.beneapp.sample.BrokenLinksMain
 * -Dexec.classpathScope=test
 */
public class BrokenLinksMain {

    public static void main(String[] args) {
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        Page page = browser.newContext().newPage();
        page.setDefaultTimeout(30000);

        try {
            // 1) open the Google home page and run a sample search
            page.navigate("https://www.google.com");
            page.locator("[name='q']").fill("Playwright automation");
            page.locator("[name='q']").press("Enter");
            page.waitForLoadState();

            // 2) collect every link on the page
            List<Locator> links = page.locator("a").all();
            System.out.println("Found " + links.size() + " links on the page");

            List<String> brokenLinks = new ArrayList<>();
            // 3) check each link's HTTP status; broken if >= 400 or it fails

            for (Locator link : links) {
                String url = link.getAttribute("href");
                if (url != null && url.startsWith("http")) {
                    // http:sdfbhadb/asdfad

                    APIResponse response = page.request().get(url);
                    int status = response.status();

                    if (status >= 400) {
                        brokenLinks.add(url);
                        System.out.println("BROKEN " + status + "  " + url);
                    }
                }

            }

            System.out.println("Broken Link = " + brokenLinks);
        } finally {
            browser.close();
            playwright.close();
        }
    }
}
