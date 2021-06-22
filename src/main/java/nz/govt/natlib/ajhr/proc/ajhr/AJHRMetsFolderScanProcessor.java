package nz.govt.natlib.ajhr.proc.ajhr;

import freemarker.template.Template;
import nz.govt.natlib.ajhr.metadata.MetadataMetProp;
import nz.govt.natlib.ajhr.metadata.MetadataRetVal;
import nz.govt.natlib.ajhr.metadata.ResultOverview;
import nz.govt.natlib.ajhr.proc.MetsTemplateService;
import nz.govt.natlib.ajhr.util.PrettyPrinter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;

public class AJHRMetsFolderScanProcessor {
    private static final Logger log = LoggerFactory.getLogger(AJHRMetsFolderScanProcessor.class);

    private final boolean isEnable;
    private int maxThreads = 1;
    private final String srcDir;
    private final String destDir;
    private final boolean isForcedReplaced;
    private int startYear = 0, endYear = 9999;

    private final Semaphore semaphore;
    private final ResultOverview overview = new ResultOverview();

    private Template metsTemplate;

    public AJHRMetsFolderScanProcessor(boolean isEnable, String srcDir, String destDir, int maxThreads, boolean isForcedReplaced, int startYear, int endYear, MetsTemplateService metsTemplateService) {
        this.isEnable = isEnable;
        this.srcDir = srcDir;
        this.destDir = destDir;
        this.maxThreads = maxThreads;
        this.isForcedReplaced = isForcedReplaced;
        this.startYear = startYear;
        this.endYear = endYear;

        semaphore = new Semaphore(maxThreads);
        try {
            metsTemplate = metsTemplateService.loadTemplate();
        } catch (IOException e) {
            log.error("Failed to generate SIP:", e);
        }
        overview.clear();

    }

    public void walkSourceFolder() throws InterruptedException, IOException {
        if (!isEnable) {
            PrettyPrinter.info(log, "AJHR processing is disabled.");
            return;
        }

        if (this.isForcedReplaced) {
            File fDestDir = new File(this.destDir);
            if (fDestDir.exists()) {
                File[] files = fDestDir.listFiles();
                if (files != null) {
                    PrettyPrinter.debug(log, "Will remove all existing contents from: " + this.destDir);
                    for (File f : files) {
                        if (f.isDirectory()) {
                            FileUtils.deleteDirectory(f);
                            PrettyPrinter.debug(log, "Deleted directory: " + f.getAbsolutePath());
                        } else {
                            FileUtils.forceDelete(f);
                            PrettyPrinter.debug(log, "Deleted file: " + f.getAbsolutePath());
                        }
                    }
                    PrettyPrinter.debug(log, "Removed all existing contents from: " + this.destDir);
                }
            } else if (!fDestDir.mkdirs()) {
                PrettyPrinter.error(log, "Failed to create dest directory: " + fDestDir.getAbsolutePath());
                return;
            }
        }
        _walkSourceFolder(new File(srcDir));

        semaphore.acquire(maxThreads);

        String resultInfo = overview.getSummaryInfo();
        PrettyPrinter.println(resultInfo);
        overview.clear();
    }

    private void _walkSourceFolder(File directory) throws InterruptedException {
        if (!directory.isDirectory()) {
            return;
        }

        if (isValidRootFolder(directory)) {
            log.debug("Found valid root directory: {}", directory.getAbsolutePath());
            listAccrualFolders(directory);
        }

        File[] subFolders = directory.listFiles();
        if (subFolders == null) {
            log.error("The root directory is empty: {}", directory.getAbsolutePath());
            return;
        }
        for (File subFolder : subFolders) {
            _walkSourceFolder(subFolder);
        }
    }

    private void listAccrualFolders(File directory) throws InterruptedException {
        File[] subFolders = directory.listFiles();
        if (subFolders == null) {
            return;
        }
        for (File subFolder : subFolders) {
            if (!subFolder.isDirectory() || !isValidSubFolder(subFolder)) {
                log.error("Skipped invalid subfolder: {}", subFolder.getAbsolutePath());
                continue;
            }

            //Try to get a token to prevent the concurrent threads not exceed the capacity threshold.
            semaphore.acquire();

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
                                AJHRMetsGenerationHandler generationProcessor = new AJHRMetsGenerationHandler(metsTemplate, directory, subFolder, destDir, isForcedReplaced);
                                retVal = generationProcessor.process();
                            } catch (IOException e) {
                                log.error("Failed to generate SIP for: {}", subFolder.getAbsolutePath(), e);
                            }
                        }

                        PrettyPrinter.printResult(retVal, subFolder.getAbsolutePath());

                        overview.addResultItem(retVal, subFolder);
                    } finally {
                        semaphore.release();
                    }
                }
            });
            t.start();
        }
    }

    public boolean isValidSubFolder(File directory) {
        File pmFolder = new File(directory, AJHRMetsGenerationHandler.PRESERVATION_MASTER_FOLDER);
        File mmFolder = new File(directory, AJHRMetsGenerationHandler.MODIFIED_MASTER_FOLDER);

        return pmFolder.exists() && pmFolder.isDirectory() && mmFolder.exists() && mmFolder.isDirectory();
    }

    public boolean isValidRootFolder(File directory) {
        MetadataMetProp metProp = MetadataMetProp.getInstance(directory.getName(), "");
        if (metProp == null) {
            return false;
        }

        int year = Integer.parseInt(metProp.getYear());

        return year >= this.startYear && year <= this.endYear;
    }
}
