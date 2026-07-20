package qa.beneapp.sample;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * ============================================================================
 *  INTERVIEW PROGRAM 2 : Read data from Excel (.xlsx) with Apache POI (XSSF)
 * ============================================================================
 *
 * QUESTION: "How do you read test data from an Excel file?"
 *
 * IDEA (say this in an interview):
 *   - Apache POI reads Office files. For the modern .xlsx format we use the
 *     XSSF classes (XSSFWorkbook). (The old .xls format uses HSSF; WorkbookFactory
 *     auto-detects either.)
 *   - Open the workbook, pick a sheet, treat ROW 0 as the column HEADERS, then
 *     for every following row read each cell and build a Map<header, value>.
 *   - Use DataFormatter to turn any cell (number, text, boolean, date) into the
 *     string exactly as shown in Excel - avoids "getStringCellValue" type errors.
 *
 * This demo WRITES a small sample accounts.xlsx first (so it runs with no file
 * to ship), then READS it back, prints it, and stores it as List<Map<String,String>>.
 * In a real project you skip the "write" part and just point readExcel(...) at
 * your existing test-data .xlsx.
 *
 * Run:
 *   mvn exec:java -Dexec.mainClass=qa.beneapp.sample.ExcelReaderMain -Dexec.classpathScope=test
 */
public class ExcelReaderMain {

    public static void main(String[] args) throws Exception {
        Path file = Paths.get("target/testdata/accounts.xlsx");

        // ---- arrange: create a sample .xlsx so the demo is self-contained ----
        writeSampleExcel(file);

        // ---- the actual lesson: READ the .xlsx into a List<Map> ----
        List<Map<String, String>> rows = readExcel(file, "Accounts");

        // ---- print what we read ----
        System.out.println("\n==== Rows read from " + file + " ====");
        for (int i = 0; i < rows.size(); i++) {
            System.out.println("Row " + (i + 1) + ": " + rows.get(i));
        }
        System.out.println("Total data rows: " + rows.size());

        // Example of using the map data (like feeding a test):
        String firstEmail = rows.get(0).get("email");
        System.out.println("\nFirst account's email (from map) = " + firstEmail);
    }

    /**
     * Reads a sheet into a List of Maps: row 0 is the header, each later row
     * becomes one Map keyed by the header text.
     */
    public static List<Map<String, String>> readExcel(Path file, String sheetName) throws Exception {
        List<Map<String, String>> data = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();   // formats any cell type to String

        try (FileInputStream fis = new FileInputStream(file.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {          // XSSF = .xlsx

            Sheet sheet = workbook.getSheet(sheetName);            // or getSheetAt(0)

            // 1) read the header row (row index 0).
            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(formatter.formatCellValue(cell).trim());
            }

            // 2) read every data row (index 1 .. last) into a Map<header, value>.
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                Map<String, String> record = new LinkedHashMap<>();
                for (int c = 0; c < headers.size(); c++) {
                    Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    record.put(headers.get(c), cell == null ? "" : formatter.formatCellValue(cell).trim());
                }
                data.add(record);
            }
        }
        return data;
    }

    /** Creates a small sample .xlsx (columns aligned to the bene-app accounts). */
    private static void writeSampleExcel(Path file) throws Exception {
        Files.createDirectories(file.getParent());
        String[] headers = { "accountNumber", "accountHolderName", "email", "totalBalance", "status" };
        String[][] data = {
                { "401K-80001", "Grace Hopper", "grace_80001@bene.com", "15000.00", "ACTIVE" },
                { "401K-80002", "Alan Turing", "alan_80002@bene.com", "28000.50", "ACTIVE" },
                { "401K-80003", "Ada Lovelace", "ada_80003@bene.com", "9200.75", "SUSPENDED" },
        };

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Accounts");
            Row header = sheet.createRow(0);
            for (int c = 0; c < headers.length; c++) {
                header.createCell(c).setCellValue(headers[c]);
            }
            for (int r = 0; r < data.length; r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < data[r].length; c++) {
                    row.createCell(c).setCellValue(data[r][c]);
                }
            }
            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                workbook.write(fos);
            }
        }
        System.out.println("Sample Excel written -> " + file.toAbsolutePath());
    }
}
