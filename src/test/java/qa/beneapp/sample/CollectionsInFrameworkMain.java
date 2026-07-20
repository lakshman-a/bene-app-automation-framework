package qa.beneapp.sample;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ============================================================================
 *  INTERVIEW ANSWER : "Where do you use Collections in your framework?"
 * ============================================================================
 *
 * SAY THIS:
 *   "All the time. A MAP holds one record as key->value (one account, one config,
 *    one DB/API row). A LIST holds many similar things in order (all rows, all
 *    links, all web elements). A LIST<MAP> represents a whole table / result set -
 *    that is exactly what Cucumber DataTable.asMaps(), my DBUtil.query() and my
 *    Excel reader return. A SET I use when I only want unique values (dedupe)."
 *
 * This tiny main shows all four with bene-app account data. No browser, no DB -
 * just plain Java so it is easy to run and explain.
 *
 * Run:
 *   mvn exec:java -Dexec.mainClass=qa.beneapp.sample.CollectionsInFrameworkMain -Dexec.classpathScope=test
 */
public class CollectionsInFrameworkMain {

    public static void main(String[] args) {

        // 1) MAP = ONE record as key -> value.
        //    Used for: one account's fields, config properties, one API/DB row.
        Map<String, String> account = new LinkedHashMap<>();
        account.put("accountNumber", "401K-80001");
        account.put("holder", "Grace Hopper");
        account.put("email", "grace@bene.com");
        account.put("status", "ACTIVE");
        System.out.println("MAP (one record)      : " + account);
        System.out.println("  read one field      : email = " + account.get("email"));

        // 2) LIST = MANY similar things, in order.
        //    Used for: all rows, all links on a page, all matching web elements.
        List<String> accountNumbers = new ArrayList<>();
        accountNumbers.add("401K-80001");
        accountNumbers.add("401K-80002");
        accountNumbers.add("401K-80003");
        System.out.println("\nLIST (many values)    : " + accountNumbers);
        System.out.println("  how many            : " + accountNumbers.size());

        // 3) LIST<MAP> = a whole TABLE / result set (one Map per row).
        //    This is EXACTLY what DataTable.asMaps(), DBUtil.query() and the
        //    Excel reader return in this framework.
        Map<String, String> account2 = new LinkedHashMap<>();
        account2.put("accountNumber", "401K-80002");
        account2.put("holder", "Alan Turing");
        account2.put("email", "alan@bene.com");
        account2.put("status", "ACTIVE");

        Map<String, String> account3 = new LinkedHashMap<>();
        account3.put("accountNumber", "401K-80003");
        account3.put("holder", "Ada Lovelace");
        account3.put("email", "ada@bene.com");
        account3.put("status", "SUSPENDED");

        List<Map<String, String>> accounts = new ArrayList<>();
        accounts.add(account);
        accounts.add(account2);
        accounts.add(account3);
        System.out.println("\nLIST<MAP> (a table)   :");
        for (Map<String, String> r : accounts) {
            System.out.println("  " + r.get("accountNumber") + " | " + r.get("holder") + " | " + r.get("status"));
        }

        // 4) SET = UNIQUE values only (duplicates dropped).
        //    Used for: unique statuses, de-duping links / options.
        Set<String> uniqueStatuses = new HashSet<>();
        for (Map<String, String> r : accounts) {
            uniqueStatuses.add(r.get("status"));
        }
        System.out.println("\nSET (unique values)   : " + uniqueStatuses);
    }
}
