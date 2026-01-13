package nz.govt.natlib.ajhr.proc;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import nz.govt.natlib.ajhr.metadata.MetadataMetProp;
import nz.govt.natlib.ajhr.metadata.MetadataRetVal;
import nz.govt.natlib.ajhr.metadata.PapersPastTitle;
import nz.govt.natlib.ajhr.util.AJHRUtils;
import nz.govt.natlib.ajhr.util.PrettyPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.Semaphore;

@Service
public class MetsFolderScanProcessor {
    private static final Logger log = LoggerFactory.getLogger(MetsFolderScanProcessor.class);
    private int maxThreads = 1;
    private String srcDir;
    private String spreadsheet;
    private int sheetNumber;
    private String destDir;
    private boolean reprocess;
    private String pickups;
//    private int startDate;
//    private int endDate;
    private boolean isForcedReplaced;
    private PapersPastTitle papersPastTitle;
    @Autowired
    private MetsTemplateService metsTemplateService;

    private Semaphore semaphore;
    private Template metsTemplate;

    public void init() {
        semaphore = new Semaphore(maxThreads);
        try {
            metsTemplate = metsTemplateService.loadTemplate();
        } catch (IOException e) {
            log.error("Failed to generate SIP:", e);
        }
    }

    public void processSpreadsheet() throws InterruptedException, IOException, InvalidFormatException {
//        if (this.isForcedReplaced) {
//            File fDestDir = new File(this.destDir);
//            if (fDestDir.exists()) {
//                File[] files = fDestDir.listFiles();
//                if (files != null) {
//                    PrettyPrinter.debug(log, "Will remove all existing contents from: " + this.destDir);
//                    for (File f : files) {
//                        if (f.isDirectory()) {
//                            FileUtils.deleteDirectory(f);
//                            PrettyPrinter.debug(log, "Deleted directory: " + f.getAbsolutePath());
//                        } else {
//                            FileUtils.forceDelete(f);
//                            PrettyPrinter.debug(log, "Deleted file: " + f.getAbsolutePath());
//                        }
//                    }
//                    PrettyPrinter.debug(log, "Removed all existing contents from: " + this.destDir);
//                }
//            } else if (!fDestDir.mkdirs()) {
//                PrettyPrinter.error(log, "Failed to create dest directory: " + fDestDir.getAbsolutePath());
//                return;
//            }
//        }
        _processSpreadsheet();

        semaphore.acquire(maxThreads);
    }

    private void _processSpreadsheet() throws InterruptedException, IOException, InvalidFormatException {
        try (XSSFWorkbook wb = new XSSFWorkbook(new File(this.spreadsheet))) {
            Sheet sheet = wb.getSheetAt(sheetNumber);
            List<String> requiredColumns = Arrays.asList("PP Code", "IE Title", "Alma MMSID", "Submission Reason",
                    "Source directory");
            Set<String> existingColumns = new HashSet<>();

            Row titleRow = sheet.getRow(0);
            if (titleRow != null) {
                for (Cell cell : titleRow) {
                    if (cell.getCellType() == CellType.STRING) {
                        String cellValue = cell.getStringCellValue();
                        if (requiredColumns.contains(cellValue)) {
                            existingColumns.add(cellValue);
                        }
                    }
                }
            }

            for (String heading : requiredColumns) {
                if (!existingColumns.contains(heading)) {
                    PrettyPrinter.error("Column '" + heading + "'" + " does not exist in spreadsheet");
                    return;
//                    throw new RuntimeException(heading + " does not exist in spreadsheet");
                }
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null && !isRowEmpty(row)) {
                    String titleCode = row.getCell(columnName("PP Code")).toString();
                    String title = row.getCell(columnName("IE Title")).toString();
                    String mmsId = row.getCell(columnName("Alma MMSID")).toString();
                    String submissionReason = row.getCell(columnName("Submission Reason")).toString();
                    String source = row.getCell(columnName("Source directory")).toString();
                    int startDate = 0;
                    int endDate = 0;

                    if (columnName("Date Range Start") != -1 && row.getCell(columnName("Date Range Start")) != null &&
                            !Objects.equals(row.getCell(columnName("Date Range Start")).toString(), "")) {
                        startDate = Integer.parseInt(row.getCell(columnName("Date Range Start")).toString());
                    }
                    if (columnName("Date Range Finish") != -1 && row.getCell(columnName("Date Range Finish")) != null
                    && !Objects.equals(row.getCell(columnName("Date Range Finish")).toString(), "")) {
                        endDate = Integer.parseInt(row.getCell(columnName("Date Range Finish")).toString());
                    }

                    this.papersPastTitle = new PapersPastTitle(titleCode, title, mmsId, submissionReason, startDate, endDate);

                    File directory = new File(source);

                    walkSourceFolder(directory);

//                    semaphore.acquire();
//                    Thread t = new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                String objectID = row.getCell(columnName("Object Identifier")).toString();
//                                String source = row.getCell(columnName("Cascade Full Path")).toString();
//                                File srcDir = new File(source);
//                                MetadataRetVal retVal = MetadataRetVal.FAIL;
//                                int tryTimes = 3;
//
//                                while (retVal == MetadataRetVal.FAIL && tryTimes > 0) {
//                                    try {
//                                        tryTimes--;
//                                        log.debug("Found valid subfolder: {}", srcDir.getAbsolutePath());
//                                        MetsGenerationHandler generationProcessor = new MetsGenerationHandler(
//                                                metsTemplate, objectID, srcDir, destDir, isForcedReplaced, startDate,
//                                                endDate);
//                                        retVal = generationProcessor.process();
//                                    } catch (TemplateException | IOException | NoSuchAlgorithmException e) {
//                                        log.error("Failed to generate SIP for: {}", srcDir.getAbsolutePath(), e);
//                                    }
//                                }
//
//                            } catch (IOException e) {
//                                throw new RuntimeException(e);
//                            } finally {
//                                semaphore.release();
//                            }
//                        }
//                    });
//                    t.start();
                }
            }

        } catch (Exception e) {
            log.error("Error loading spreadsheet {}", this.spreadsheet);
            log.error(String.valueOf(e));
        }


    }

    private void walkSourceFolder(File directory) throws IOException, InterruptedException, InvalidFormatException {
        if (!directory.isDirectory()) {
            return;
        }
        int startYear = 1700;
        int endYear = 2100;

        if (this.papersPastTitle.startDate() != 0 && this.papersPastTitle.endDate() != 0) {
            startYear = Integer.parseInt(String.valueOf(this.papersPastTitle.startDate()).substring(0, 4));
            endYear = Integer.parseInt(String.valueOf(this.papersPastTitle.endDate()).substring(0, 4));
        }

        if (directory.getName().matches("\\d+")) {
            if (Integer.parseInt(directory.getName()) >= startYear && Integer.parseInt(directory.getName()) <= endYear) {
                log.debug("Found valid root directory: {}", directory.getAbsolutePath());
                File[] children = directory.listFiles(File::isDirectory);
                if (children == null || children.length == 0) {
                    System.out.println("No subfolders found.");
                    return;
                }
                if (!children[0].getName().matches("\\d{2}")) {
                    _processSpreadsheet(directory);
                }
            } else if (Integer.parseInt(directory.getName()) > 0 && Integer.parseInt(directory.getName()) < 13) {
                _processSpreadsheet(directory);
                return;
            } else {
                return;
            }
        }

        File[] subFolders = directory.listFiles();
        if (subFolders == null) {
            log.error("The root directory is empty: {}", directory.getAbsolutePath());
            return;
        }
        for (File subFolder : subFolders) {
            walkSourceFolder(subFolder);
        }
    }

    private void _processSpreadsheet(File directory) throws InterruptedException, IOException, InvalidFormatException {
        File[] subFolders = directory.listFiles();
        if (subFolders == null) {
            return;
        }

        for (File subFolder : subFolders) {
//            if (!subFolder.isDirectory() || !isValidSubFolder(subFolder)) {
//                log.error("Skipped invalid subfolder: {}", subFolder.getAbsolutePath());
//                continue;
//            }
            String validSubFolder = getValidSubFolder(subFolder);
            if (this.reprocess) {
                validSubFolder = validSubFolder + "_reprocess";
            }

            if (!subFolder.isDirectory() || (validSubFolder == null)) {
                log.error("Skipped invalid subfolder: {}", subFolder.getAbsolutePath());
                continue;
            }

            //Try to get a token to prevent the concurrent threads not exceed the capacity threshold.
            semaphore.acquire();

            String finalValidSubFolder = validSubFolder;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        MetadataRetVal retVal = MetadataRetVal.FAIL;
                        int tryTimes = 3;

                        while (retVal == MetadataRetVal.FAIL && tryTimes > 0) {
                            try {
                                tryTimes--;
                                log.debug("Found valid subfolder: {}", subFolder.getAbsolutePath());
                                MetsGenerationHandler generationProcessor = new MetsGenerationHandler(metsTemplate,
                                        subFolder, AJHRUtils.combinePath(destDir, finalValidSubFolder), isForcedReplaced,
                                        papersPastTitle, reprocess);
                                retVal = generationProcessor.process();
                            } catch (TemplateException | IOException | NoSuchAlgorithmException e) {
                                log.error("Failed to generate SIP for: {}", subFolder.getAbsolutePath(), e);
                            }
                        }

                        PrettyPrinter.printResult(retVal, subFolder.getAbsolutePath());
                    } finally {
                        semaphore.release();
                    }
                }
            });
            t.start();
        }
    }

//    public boolean isValidSubFolder(File directory) {
//       File pmFolder = new File(directory, MetsGenerationHandler.PRESERVATION_MASTER_FOLDER);
//       File mmFolder = new File(directory, MetsGenerationHandler.MODIFIED_MASTER_FOLDER);
//
//       return pmFolder.exists() && pmFolder.isDirectory() && mmFolder.exists() && mmFolder.isDirectory();
//    }

    public String getValidSubFolder(File directory) throws IOException {
        String current = directory.getName();

        // Case 1: Folder has style CODE_DATE (using MetadataMetProp)
        MetadataMetProp metProp = MetadataMetProp.getInstance(current, this.papersPastTitle);
        if (metProp != null) {
            String dateStr = metProp.getDate();
            if (isInteger(dateStr)) {
                int directoryDate = Integer.parseInt(dateStr);

                if (this.papersPastTitle.startDate() != 0 && this.papersPastTitle.endDate() != 0) {
                    if (directoryDate >= this.papersPastTitle.startDate() &&
                            directoryDate <= this.papersPastTitle.endDate()) {
                        if (!Objects.equals(pickups, "") && pickups != null) {
                            Path pickupsFile = Paths.get(pickups);

                            try (var lines = Files.lines(Paths.get(pickupsFile.toUri()))) {
                                boolean exists = lines.anyMatch(line -> line.contains(current));
                                return exists ? current : null;
                            } catch (IOException e) {
                                e.printStackTrace();
                                return null;
                            }
                        }
                        return current;
                    } else {
                        return null;
                    }
                }
                return current; // no range filtering
            }
            return null; // invalid date
        }

        // Case 2: Folder path is structure yyyy/mm/dd
        if (isInteger(current)) {
            int day = Integer.parseInt(current);
            if (day > 0 && day < 32) {
                File parent = directory.getParentFile();
                if (parent == null) return null;

                String parentName = parent.getName();
                if (isInteger(parentName)) {
                    int month = Integer.parseInt(parentName);
                    if (month > 0 && month < 13) {
                        File grandParent = parent.getParentFile();
                        if (grandParent == null) return null;

                        String yearStr = grandParent.getName();
                        if (isInteger(yearStr)) {
                            int year = Integer.parseInt(yearStr);
                            if (year > 1700 && year < 2100) {
                                int date = Integer.parseInt(yearStr + String.format("%02d", month) + String.format("%02d", day));
                                String folder = papersPastTitle.titleCode() + "_" + date;

                                if (this.papersPastTitle.startDate() != 0 && this.papersPastTitle.endDate() != 0) {
                                    return (date >= this.papersPastTitle.startDate() &&
                                            date <= this.papersPastTitle.endDate()) ? folder : null;
                                }
                                return folder;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    private static boolean isInteger(String s) {
        if (s == null) return false;
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isFileOnlyFolder(File folder) {
        if (folder == null || !folder.isDirectory()) {
            return false;
        }

        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            return false;
        }

        for (File f : files) {
            if (f.isDirectory()) {
                return false;
            }
        }

        return true;
    }

    public int columnName(String a) throws IOException {
        int coefficient = -1;
        Workbook wb = WorkbookFactory.create(new FileInputStream(this.spreadsheet));
        Sheet sheet = wb.getSheetAt(sheetNumber);
        Row row = sheet.getRow(0);
        int cellNum = row.getPhysicalNumberOfCells();
        for (int i = 0; i < cellNum; i++) {
            if ((row.getCell(i).toString()).equals(a)) {
                coefficient = i;
            }
        }
        return coefficient;
    }

    public static boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK)
                return false;
        }
        return true;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

//    public String getSrcDir() {
//        return srcDir;
//    }
//
//    public void setSrcDir(String srcDir) {
//        this.srcDir = srcDir;
//    }

    public String getSpreadsheet() {
        return spreadsheet;
    }

    public void setSpreadsheet(String spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    public String getDestDir() {
        return destDir;
    }

    public void setDestDir(String destDir) {
        this.destDir = destDir;
    }

    public boolean isForcedReplaced() {
        return isForcedReplaced;
    }

    public void setForcedReplaced(boolean forcedReplaced) {
        isForcedReplaced = forcedReplaced;
    }

    public int getSheetNumber() {
        return sheetNumber;
    }

    public void setSheetNumber(int sheetNumber) {
        this.sheetNumber = sheetNumber;
    }

    public boolean isReprocess() {
        return reprocess;
    }

    public void setReprocess(boolean reprocess) {
        this.reprocess = reprocess;
    }

    public String getPickups() {
        return pickups;
    }

    public void setPickups(String pickups) {
        this.pickups = pickups;
    }
}
