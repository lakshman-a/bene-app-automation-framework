package qa.beneapp.sample;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Worker;

/**
 * ============================================================================
 *  WEB WORKER  (the browser-side Worker - this one DOES exist in Java)
 * ============================================================================
 *
 * THE CONFUSION THIS CLASS KILLS:
 *   The word "worker" means TWO completely unrelated things in Playwright.
 *
 *   1) TEST RUNNER WORKER  -> "workers: 4" in playwright.config.ts
 *      Parallel OS processes that run test files at the same time.
 *      This is part of the @playwright/test runner => TS/JS ONLY.
 *      NOT in Java, and never will be. In Java we get parallelism from
 *      Surefire / TestNG / Cucumber instead.
 *
 *   2) WEB WORKER  -> new Worker('script.js') in the PAGE's JavaScript
 *      A background thread the WEB APP itself starts so heavy JS does not
 *      freeze the UI. This is a browser feature, not a test feature.
 *      Playwright Java CAN see and drive these: Page.workers(),
 *      Page.onWorker(), Page.waitForWorker(), Worker.evaluate().
 *      Available "before v1.9" - so yes, it is in our 1.42.0.
 *
 *   The cheat sheet says "workers - not built into Java" = meaning (1).
 *   The docs page showing Page.workers() = meaning (2). BOTH ARE CORRECT.
 *
 * WHAT THIS DEMO DOES:
 *   Builds a tiny page (no server needed) whose JavaScript starts a real Web
 *   Worker to add up 1..1000 off the main thread. We then attach to that
 *   worker from Java and run code INSIDE it.
 *
 * THE PROOF THAT SELLS IT IN AN INTERVIEW:
 *   worker.evaluate("() => typeof document")  ->  "undefined"
 *   A Web Worker has NO document and NO window - it is a separate JS context.
 *   That single line proves we are executing inside the worker, not the page.
 *
 * WHY A QA WOULD ACTUALLY CARE:
 *   - apps push heavy work (crypto, parsing, big table sorting, PDF render)
 *     into workers; if a worker throws, the UI just silently stops updating
 *   - you can assert the worker exists, inspect its state, and catch the
 *     "worker died" class of bug that a normal UI assertion never sees
 *
 * Run:
 *   mvn exec:java -Dexec.mainClass=qa.beneapp.sample.WebWorkerMain -Dexec.classpathScope=test
 */
public class WebWorkerMain {

    /**
     * A self-contained page. The worker source is built at runtime into a Blob
     * URL, so this demo needs no web server and no bene-app.
     *
     * Note: "\\n" below is deliberate. In a Java text block "\n" would become a
     * real newline and break the JS string literal; "\\n" reaches JS as \n.
     */
    private static final String HTML = """
            <html>
              <body>
                <h1>Web Worker demo</h1>
                <div id="result">waiting...</div>
                <script>
                  var workerSrc = [
                    'self.onmessage = function (e) {',
                    '  var n = e.data, sum = 0;',
                    '  for (var i = 1; i <= n; i++) sum += i;',
                    '  self.postMessage(sum);',
                    '};'
                  ].join('\\n');

                  var blob = new Blob([workerSrc], { type: 'application/javascript' });
                  var worker = new Worker(URL.createObjectURL(blob));

                  worker.onmessage = function (e) {
                    document.getElementById('result').textContent = 'Sum = ' + e.data;
                  };

                  worker.postMessage(1000);
                </script>
              </body>
            </html>
            """;

    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(300));
            Page page = browser.newContext().newPage();

            try {
                // STEP 1: listen for ANY worker the page spawns (fire-and-forget event).
                page.onWorker(w -> System.out.println("[event] worker created -> " + w.url()));

                // STEP 2: waitForWorker takes the ACTION as a callback and hands back the
                //         Worker that action spawned. This avoids a race: the worker is
                //         born while setContent runs, so we cannot subscribe afterwards.
                Worker worker = page.waitForWorker(() -> page.setContent(HTML));

                System.out.println("Worker URL        : " + worker.url());
                System.out.println("page.workers()    : " + page.workers().size() + " live worker(s)");

                // STEP 3: evaluate() runs INSIDE the worker's own JS context.
                System.out.println("1 + 1 in worker   : " + worker.evaluate("() => 1 + 1"));
                System.out.println("typeof self       : " + worker.evaluate("() => typeof self"));
                System.out.println("typeof document   : " + worker.evaluate("() => typeof document")
                        + "   <-- undefined proves this is NOT the page context");

                // STEP 4: the worker posted its result back; the page rendered it.
                page.waitForCondition(() -> page.locator("#result").textContent().startsWith("Sum"));
                String rendered = page.locator("#result").textContent();
                System.out.println("Page shows        : " + rendered);

                boolean ok = "Sum = 500500".equals(rendered);
                System.out.println("RESULT: worker computed 1..1000 correctly = " + ok);
            } finally {
                browser.close();
            }
        }
    }
}
