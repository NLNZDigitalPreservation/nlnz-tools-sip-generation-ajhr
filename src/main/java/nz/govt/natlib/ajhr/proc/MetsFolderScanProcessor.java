package nz.govt.natlib.ajhr.proc;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import nz.govt.natlib.ajhr.metadata.MetadataMetProp;
import nz.govt.natlib.ajhr.metadata.MetadataRetVal;
import nz.govt.natlib.ajhr.util.LogPrinter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;

public class MetsFolderScanProcessor {
    @Value("${maxThreads}")
    private int maxThreads;
    @Value("${sourceFolder}")
    private String sourceFolder;
    @Value("${targetFolder}")
    private String targetFolder;
    @Value("${isForcedReloaded}")
    private boolean isForcedReloaded;
    @Autowired
    private MetsTemplateService metsTemplateService;

    private Semaphore semaphore;
    private Template metsTemplate;

    @PostConstruct
    public void init() {
        semaphore = new Semaphore(maxThreads);
        try {
            metsTemplate = metsTemplateService.loadTemplate();
        } catch (IOException e) {
            LogPrinter.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public void walkSourceFolder() throws InterruptedException {
        _walkSourceFolder(new File(sourceFolder));
    }

    private void _walkSourceFolder(File directory) throws InterruptedException {
        if (!directory.isDirectory()) {
            LogPrinter.debug("Skipped invalid root directory: " + directory.getAbsolutePath());
            return;
        }

        if (isValidRootFolder(directory)) {
            LogPrinter.debug("Found valid root directory: " + directory.getAbsolutePath());
            listAccrualFolders(directory);
        }

        File[] subFolders = directory.listFiles();
        if (subFolders == null) {
            LogPrinter.error("The root directory is empty: " + directory.getAbsolutePath());
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
                LogPrinter.error("Skipped invalid subfolder: " + subFolder.getAbsolutePath());
                continue;
            }

            //Try to get a token to prevent the concurrent threads not exceed the capacity threshold.
            semaphore.acquire();

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        MetadataRetVal retVal = MetadataRetVal.FAILED;
                        int tryTimes = 3;

                        while (retVal == MetadataRetVal.FAILED && tryTimes > 0) {
                            try {
                                tryTimes--;
                                LogPrinter.debug("Found valid subfolder: " + subFolder.getAbsolutePath());
                                MetsGenerationHandler generationProcessor = new MetsGenerationHandler(metsTemplate, directory, subFolder, targetFolder, isForcedReloaded);
                                retVal = generationProcessor.process();
                            } catch (TemplateException | IOException e) {
                                LogPrinter.error(ExceptionUtils.getStackTrace(e));
                            }
                        }

                        LogPrinter.info(retVal.name() + ": " + subFolder.getAbsolutePath());
                    } finally {
                        semaphore.release();
                    }
                }
            });
            t.start();
        }
    }

    public boolean isValidSubFolder(File directory) {
        File pmFolder = new File(directory, MetsGenerationHandler.PRESERVATION_MASTER_FOLDER);
        File mmFolder = new File(directory, MetsGenerationHandler.MODIFIED_MASTER_FOLDER);

        return pmFolder.exists() && pmFolder.isDirectory() && mmFolder.exists() && mmFolder.isDirectory();
    }

    public boolean isValidRootFolder(File directory) {
        MetadataMetProp metProp = MetadataMetProp.getInstance(directory.getName(), "");
        return metProp != null;
    }
}
