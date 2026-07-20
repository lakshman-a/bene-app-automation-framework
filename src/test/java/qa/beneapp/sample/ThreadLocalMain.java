package qa.beneapp.sample;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

/**
 * ============================================================================
 *  INTERVIEW ANSWER : "How do you run your tests in PARALLEL?"
 * ============================================================================
 *
 * SAY THIS:
 *   "I run tests in parallel using the Playwright ThreadLocal concept. Playwright,
 *    Browser and Page are NOT thread-safe to share, so at framework level I keep
 *    them in ThreadLocal - that gives EACH thread its OWN browser and page. The
 *    test runner (Cucumber/TestNG/JUnit with parallel threads) simply starts many
 *    threads; each thread creates and uses its own isolated browser, so tests run
 *    at the same time without stepping on each other."
 *
 * This tiny main SIMULATES that: instead of a real runner, two plain Java threads
 * run the SAME code in parallel. Each thread gets its own Playwright/Browser/Page
 * from ThreadLocal - exactly what a parallel framework does under the hood.
 *
 * Run:
 *   mvn exec:java -Dexec.mainClass=qa.beneapp.sample.ThreadLocalMain -Dexec.classpathScope=test
 */
public class ThreadLocalMain {

    // "one box PER THREAD": each thread that calls .get() sees only what IT .set().
    static ThreadLocal<Playwright> playwrightTL = new ThreadLocal<>();
    static ThreadLocal<Browser> browserTL = new ThreadLocal<>();
    static ThreadLocal<Page> pageTL = new ThreadLocal<>();

    public static void main(String[] args) throws Exception {

        // The SAME work both threads run. Because everything is stored in
        // ThreadLocal, thread-1 and thread-2 never share a browser or a page.
        Runnable task = () -> {
            String name = Thread.currentThread().getName();
            System.out.println(name + " started");

            // ---- these are the normal setup lines, now made PER-THREAD ----
            playwrightTL.set(Playwright.create());                                             // each thread: its own Playwright
            browserTL.set(playwrightTL.get().chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true)));                       // each thread: its own Browser
            pageTL.set(browserTL.get().newContext().newPage());                               // each thread: its own Page
            pageTL.get().setDefaultTimeout(30000);

            // use THIS thread's own page - runs at the same time as the other thread
            pageTL.get().navigate("http://localhost:4200/login");
            System.out.println(name + " -> title = " + pageTL.get().title());

            // each thread closes ITS OWN objects
            pageTL.get().close();
            browserTL.get().close();
            playwrightTL.get().close();
            System.out.println(name + " finished");
        };

        // A real framework's parallel runner starts many threads; here we start two.
        Thread t1 = new Thread(task, "Thread-1");
        Thread t2 = new Thread(task, "Thread-2");
        t1.start();
        t2.start();

        // wait for both parallel runs to complete
        t1.join();
        t2.join();
        System.out.println("Both parallel threads finished.");
    }
}
