package nz.govt.natlib.ajhr.proc;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import nz.govt.natlib.ajhr.metadata.MetadataMetProp;
import nz.govt.natlib.ajhr.metadata.MetadataRetVal;
import nz.govt.natlib.ajhr.util.PrettyPrinter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Semaphore;

@Service
public class MetsFolderScanProcessor {
    private static final Logger log = LoggerFactory.getLogger(MetsFolderScanProcessor.class);
    private int maxThreads = 1;
    private String srcDir;
    private String destDir;
    private boolean isForcedReplaced;
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

    public void walkSourceFolder() throws InterruptedException, IOException {
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
                                MetsGenerationHandler generationProcessor = new MetsGenerationHandler(metsTemplate, directory, subFolder, destDir, isForcedReplaced);
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

    public boolean isValidSubFolder(File directory) {
        File pmFolder = new File(directory, MetsGenerationHandler.PRESERVATION_MASTER_FOLDER);
        File mmFolder = new File(directory, MetsGenerationHandler.MODIFIED_MASTER_FOLDER);

        return pmFolder.exists() && pmFolder.isDirectory() && mmFolder.exists() && mmFolder.isDirectory();
    }

    public boolean isValidRootFolder(File directory) {
        MetadataMetProp metProp = MetadataMetProp.getInstance(directory.getName(), "");
        return metProp != null;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public String getSrcDir() {
        return srcDir;
    }

    public void setSrcDir(String srcDir) {
        this.srcDir = srcDir;
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
}
