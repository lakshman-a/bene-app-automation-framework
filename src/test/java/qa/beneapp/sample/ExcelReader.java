package qa.beneapp.sample;

import java.io.FileInputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Simplest possible Excel reader (interview style): open an .xlsx that is on the
 * Desktop, load the workbook and the first sheet, then loop rows and cells and
 * print everything. No headers, no maps, no helper methods - all inside main().
 *
 * Run:
 *   mvn exec:java -Dexec.mainClass=qa.beneapp.sample.ExcelReader -Dexec.classpathScope=test
 */
public class ExcelReader {

    public static void main(String[] args) throws Exception {
        // 1) the Excel file sitting on the Desktop
        String path = "C:\\Users\\minto\\OneDrive\\Desktop\\accounts.xlsx";

        // 2) load the file and the workbook (.xlsx -> XSSF)
        FileInputStream fis = new FileInputStream(path);
        XSSFWorkbook workbook = new XSSFWorkbook(fis);

        // 3) pick the first sheet
        Sheet sheet = workbook.getSheetAt(0);

        // 4) read every row and every cell, and print (cell.toString() = value as text)
        for (Row row : sheet) {
            for (Cell cell : row) {
                System.out.print(cell.toString() + "\t");
            }
            System.out.println();
        }

        // 5) close
        workbook.close();
        fis.close();
    }
}
