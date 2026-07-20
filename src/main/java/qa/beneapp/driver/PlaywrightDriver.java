package qa.beneapp.driver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import qa.beneapp.config.ConfigManager;

/**
 * PlaywrightDriver - manages the Playwright Browser and Page lifecycle.
 *
 * Single-threaded by design: one Playwright/Browser/Context/Page per run,
 * held in plain static fields (no ThreadLocal / parallelism). This keeps the
 * flow simple and predictable for a sequential test suite.
 *
 * All browser settings come from the active config-&lt;env&gt;.properties file,
 * with an optional -Dbrowser / -Dheadless CLI override.
 */
public class PlaywrightDriver {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightDriver.class);

    private static Playwright playwright;
    private static Browser browser;
    private static BrowserContext context;
    private static Page page;

    private PlaywrightDriver() {
    }

    /** Get the current Page, creating the browser if needed. */
    public static Page getPage() {
        if (page == null) {
            initDriver();
        }
        return page;
    }

    /** True when a browser page is active (i.e. a UI scenario is running). */
    public static boolean hasPage() {
        return page != null;
    }

    /** Initialize Playwright, Browser, Context and Page from config. */
    public static void initDriver() {
        log.info("=== Initializing Playwright Driver ===");

        String browserType = System.getProperty("browser", ConfigManager.get("browser.type", "chromium"));
        boolean headless = Boolean.parseBoolean(
                System.getProperty("headless", ConfigManager.get("browser.headless", "false")));
        double slowMo = Double.parseDouble(ConfigManager.get("browser.slow.mo", "0"));
        double timeout = Double.parseDouble(ConfigManager.get("browser.timeout", "30000"));
        int width = ConfigManager.getInt("viewport.width", 1920);
        int height = ConfigManager.getInt("viewport.height", 1080);

        log.info("Browser: {} | Headless: {} | SlowMo: {}ms | Timeout: {}ms",
                browserType, headless, slowMo, timeout);

        playwright = Playwright.create();

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setSlowMo(slowMo);

        switch (browserType.toLowerCase()) {
            case "firefox":
                browser = playwright.firefox().launch(launchOptions);
                break;
            case "webkit":
                browser = playwright.webkit().launch(launchOptions);
                break;
            default:
                browser = playwright.chromium().launch(launchOptions);
                break;
        }

        context = browser.newContext(new Browser.NewContextOptions().setViewportSize(width, height));
        context.setDefaultTimeout(timeout);

        page = context.newPage();
        log.info("Playwright Driver initialized successfully");
    }

    /** Close and clean up all Playwright resources. */
    public static void quitDriver() {
        log.info("=== Closing Playwright Driver ===");
        try {
            if (page != null) { page.close(); page = null; }
            if (context != null) { context.close(); context = null; }
            if (browser != null) { browser.close(); browser = null; }
            if (playwright != null) { playwright.close(); playwright = null; }
            log.info("Playwright Driver closed successfully");
        } catch (Exception e) {
            log.error("Error closing Playwright Driver: {}", e.getMessage());
        }
    }

    /** Take a full-page screenshot (e.g. for Allure attachment). */
    public static byte[] takeScreenshot() {
        if (page != null) {
            return page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
        }
        return new byte[0];
    }
}
