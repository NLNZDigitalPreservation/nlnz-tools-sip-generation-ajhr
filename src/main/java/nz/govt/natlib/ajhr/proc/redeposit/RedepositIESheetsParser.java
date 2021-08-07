package nz.govt.natlib.ajhr.proc.redeposit;

import nz.govt.natlib.ajhr.metadata.RedepositIeDTO;
import nz.govt.natlib.ajhr.metadata.RedepositIeFileDTO;
import nz.govt.natlib.ajhr.util.MetsUtils;
import nz.govt.natlib.ajhr.util.PrettyPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedepositIESheetsParser {
    public static final Logger log = LoggerFactory.getLogger(RedepositIESheetsParser.class);
    private static final String userDirectory = System.getProperty("user.dir");
    private final static File SHEET_FILE = MetsUtils.combinePath(userDirectory, "conf", "resubmission", "Input spreadsheet for redeposit of IE with bad SM Mids.xlsx");

    public static List<RedepositIeDTO> parse(String sheetName, boolean isMultipleRowsExtension) throws IOException {
        PrettyPrinter.info("Start to parse the sheet: {}, isMultipleRowsExtension: {}", sheetName, Boolean.toString(isMultipleRowsExtension));
        List<RedepositIeDTO> retVal = new ArrayList<>();

        InputStream inputStream = new FileInputStream(SHEET_FILE);
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheet(sheetName);

        try {
            Map<Integer, String> headerIndex = new HashMap<>();
            Row fieldNameRow = sheet.getRow(1);
            for (int col = 0; col <= fieldNameRow.getLastCellNum(); col++) {
                Cell cell = fieldNameRow.getCell(col);
                if (cell == null) {
                    break;
                }
                String colValue = cell.toString();
                headerIndex.put(col, colValue);
            }

            for (int rowNum = 2; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) {
                    continue;
                }
                RedepositIeDTO dto = new RedepositIeDTO();

                for (int col = 0; col < headerIndex.size(); col++) {
                    Cell cell = row.getCell(col);
                    String colKey = headerIndex.get(col);
                    dto.setValue(colKey, cell);
                }

                //Ignore empty rows
                if (StringUtils.isEmpty(dto.getOriginalPID())) {
                    PrettyPrinter.error(log, "The row is empty, rowNumber: " + rowNum);
                    continue;
                }

                retVal.add(dto);

                int numOfFiles = dto.getNumFiles();
                if (!isMultipleRowsExtension || numOfFiles <= 1) {
                    continue;
                }

                for (int fNum = 1; fNum <= numOfFiles; fNum++) {
                    Row fileRow = sheet.getRow(rowNum + fNum);
                    if (fileRow == null) {
                        break;
                    }
                    RedepositIeFileDTO fileDTO = new RedepositIeFileDTO();
                    for (int col = 0; col < headerIndex.size(); col++) {
                        Cell cell = fileRow.getCell(col);
                        if (cell == null) {
                            continue;
                        }
                        String colKey = headerIndex.get(col);
                        String colValue = cell.toString();
                        fileDTO.setValue(colKey, colValue);
                    }

                    if (!fileDTO.getOriginalPID().contains("-")) {
                        throw new IOException(String.format("[%s]: Invalid sub-item: %s, in line: %d", dto.getOriginalPID(), fileDTO.getOriginalPID(), (rowNum + fNum + 1)));
                    }

                    fileDTO.setFileId(fNum);
                    dto.getFiles().add(fileDTO);
                }

                rowNum += numOfFiles;
            }
        } finally {
            inputStream.close();
            PrettyPrinter.info("Finished parse sheet: {}, rows found: {}", sheetName, Integer.toString(retVal.size()));
        }
        return retVal;
    }

    private static int parseInt(String strVal) {
        if (StringUtils.isEmpty(strVal)) {
            return 0;
        }
        try {
            return (int) Double.parseDouble(strVal.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static String getValueFromCell(Cell cell) {
        CellType type = cell.getCellType();
        String value;
        switch (cell.getCellType()) {
            case STRING:
            case BLANK:
                value = cell.getStringCellValue();
                break;
            case NUMERIC:
                value = String.format("%.0f", cell.getNumericCellValue());
                break;
            case BOOLEAN:
                value = Boolean.toString(cell.getBooleanCellValue());
                break;
            default:
                value = "";
        }
        value = value == null ? "" : value;
        return value;
    }
}
