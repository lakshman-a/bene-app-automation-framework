package qa.beneapp.sample;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.WebSocket;

/**
 * ============================================================================
 *  WEBSOCKETS  (observing live socket traffic - available in Java 1.42)
 * ============================================================================
 *
 * WHAT A WEBSOCKET IS (vs the REST calls we already test):
 *   REST  = we ask, server answers, connection ends.   (request/response)
 *   WS    = one connection stays OPEN and BOTH sides push messages any time.
 *   Used for: live prices, chat, notifications, "balance updated" toasts.
 *
 * WHAT PLAYWRIGHT JAVA 1.42 CAN DO  -> OBSERVE
 *   page.onWebSocket(...)        - fires when the app opens a socket
 *   page.waitForWebSocket(...)   - run an action, get back the socket it opened
 *   ws.onFrameSent(...)          - every message the BROWSER sends
 *   ws.onFrameReceived(...)      - every message the SERVER pushes
 *   ws.onSocketError(...) / ws.onClose(...) / ws.isClosed() / ws.url()
 *
 * WHAT IT CANNOT DO IN 1.42  -> MOCK
 *   page.routeWebSocket() (intercept + fake WS responses) landed in 1.48.
 *   Our pom is on 1.42.0, so sockets are READ-ONLY for us today. To mock a
 *   socket we would have to bump playwright.version to >= 1.48.
 *   (Verified: no routeWebSocket method exists in the 1.42.0 jar.)
 *
 * WHY A QA WOULD ACTUALLY CARE:
 *   - prove the UI really subscribed to the feed after login
 *   - assert the handshake/auth message the client sends is correct
 *   - catch a socket that silently dies and leaves stale data on screen
 *
 * NOTE ON THE TARGET:
 *   bene-app is plain REST, so it has no socket to watch. We use a PUBLIC echo
 *   server, which needs internet. It replies with whatever we send, so we can
 *   see both directions. Swap ECHO_URL for your app's real wss:// endpoint and
 *   the exact same listener code applies.
 *
 *   If this hangs with "Timeout 30000ms exceeded", the echo host is unreachable
 *   from your network - not a code bug. wss://echo.websocket.events did not
 *   resolve here, which is why we use Postman's. Check with:
 *     Test-NetConnection ws.postman-echo.com -Port 443
 *
 * Run:
 *   mvn exec:java -Dexec.mainClass=qa.beneapp.sample.WebSocketObserverMain -Dexec.classpathScope=test
 */
public class WebSocketObserverMain {

    private static final String ECHO_URL = "wss://ws.postman-echo.com/raw";

    /** A page whose JS opens a socket and sends one message once connected. */
    private static final String HTML = """
            <html>
              <body>
                <h1>WebSocket demo</h1>
                <div id="log">connecting...</div>
                <script>
                  var ws = new WebSocket('%s');

                  ws.onopen = function () {
                    ws.send('hello from the bene-app test');
                  };

                  ws.onmessage = function (e) {
                    document.getElementById('log').textContent = 'last server msg: ' + e.data;
                  };
                </script>
              </body>
            </html>
            """.formatted(ECHO_URL);

    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(300));
            Page page = browser.newContext().newPage();

            // Frames arrive on a Playwright event thread, so use a synchronized list.
            List<String> sent = new ArrayList<>();
            List<String> received = new ArrayList<>();

            try {
                // STEP 1: subscribe BEFORE the socket opens, then trigger the action.
                //         waitForWebSocket returns the socket that the action opened.
                WebSocket ws = page.waitForWebSocket(() -> {
                    page.onWebSocket(socket -> {
                        System.out.println("[ws] opened -> " + socket.url());

                        socket.onFrameSent(f -> {
                            sent.add(f.text());
                            System.out.println("   --> browser sent : " + f.text());
                        });
                        socket.onFrameReceived(f -> {
                            received.add(f.text());
                            System.out.println("   <-- server pushed: " + f.text());
                        });
                        socket.onSocketError(err -> System.out.println("[ws] ERROR: " + err));
                        socket.onClose(s -> System.out.println("[ws] closed"));
                    });
                    page.setContent(HTML);
                });

                System.out.println("Socket URL : " + ws.url());

                // STEP 2: wait until the echo comes back and the page renders it.
                page.waitForCondition(() -> page.locator("#log").textContent().startsWith("last server msg"));

                System.out.println("Page shows : " + page.locator("#log").textContent());
                System.out.println("Frames sent     : " + sent);
                System.out.println("Frames received : " + received);
                System.out.println("Socket closed?  : " + ws.isClosed());

                boolean echoed = received.stream().anyMatch(m -> m.contains("hello from the bene-app test"));
                System.out.println("RESULT: server echoed our message back = " + echoed);
            } finally {
                browser.close();
            }
        }
    }
}
