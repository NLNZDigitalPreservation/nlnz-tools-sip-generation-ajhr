package nz.govt.natlib.ajhr.proc.redeposit;

import nz.govt.natlib.ajhr.metadata.RedepositIeDTO;
import nz.govt.natlib.ajhr.metadata.RedepositIeFileDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedepositIESheetsParser {
    private final static String SHEET_FILE = "Input spreadsheet for redeposit of IE with bad SM Mids.xlsx";

    public static List<RedepositIeDTO> parse(String sheetName) throws IOException {
        List<RedepositIeDTO> retVal = new ArrayList<>();

        Resource resource = new ClassPathResource(SHEET_FILE);
        Workbook workbook = new XSSFWorkbook(resource.getInputStream());
//        Sheet sheet = workbook.getSheetAt(0);
        Sheet sheet = workbook.getSheet(sheetName);


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
            retVal.add(dto);
            for (int col = 0; col < headerIndex.size(); col++) {
                Cell cell = row.getCell(col);
                if (cell == null) {
                    continue;
                }
                String colKey = headerIndex.get(col);
                String colValue = cell.toString();
                dto.setValue(colKey, colValue);
            }

            //Ignore empty rows
            if (StringUtils.isEmpty(dto.getOriginalPID())) {
                continue;
            }

            int numOfFiles = dto.getNumFiles();
            if (numOfFiles <= 1) {
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
                dto.getFiles().add(fileDTO);
            }

            rowNum += numOfFiles;
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
        return value;
    }
}
