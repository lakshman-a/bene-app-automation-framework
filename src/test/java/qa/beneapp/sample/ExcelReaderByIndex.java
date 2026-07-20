package qa.beneapp.sample;

import java.io.FileInputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Simple Excel reader using INDEX loops (interview style): open an .xlsx on the
 * Desktop, load the workbook and first sheet, then loop rows with getLastRowNum()
 * and cells with getLastCellNum(), read each cell value and print it. No headers,
 * no maps, no helper methods - all inside main().
 *
 * Run:
 *   mvn exec:java -Dexec.mainClass=qa.beneapp.sample.ExcelReaderByIndex -Dexec.classpathScope=test
 */
public class ExcelReaderByIndex {

    public static void main(String[] args) throws Exception {
       
        // 2) load the file and the workbook (.xlsx -> XSSF)
        FileInputStream fis = new FileInputStream("C:\\Users\\minto\\OneDrive\\Desktop\\accounts.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(fis);

        // 3) pick the first sheet
        Sheet sheet = workbook.getSheetAt(0);

        // 4) loop rows by index (getLastRowNum = last row index, 0-based)
        for (int r = 0; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
                // loop cells by index (getLastCellNum = number of cells, so use <)
                for (int c = 0; c < row.getLastCellNum(); c++) {
                    Cell cell = row.getCell(c);
                    System.out.print(cell.toString());
                }
                System.out.println();

        }

        // 5) close
        workbook.close();
        fis.close();
    }
}
