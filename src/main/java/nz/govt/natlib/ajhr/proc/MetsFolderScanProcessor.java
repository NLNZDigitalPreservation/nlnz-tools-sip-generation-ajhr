package nz.govt.natlib.ajhr.proc;

import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetsFolderScanProcessor {
    private static final Pattern pattern = Pattern.compile("AJHR_18\\d{2}|19\\d{2}|20\\d{2}_.+");

    @Value("${maxThreads}")
    private int maxThreads;
    @Value("${sourceFolder}")
    private String sourceFolder;
    @Value("${targetFolder}")
    private String targetFolder;

    private ExecutorService _executor_service;

    @PostConstruct
    public void init() {
        _executor_service = Executors.newFixedThreadPool(maxThreads);
    }


    public void walkSourceFolder() {
        _walkSourceFolder(new File(sourceFolder));
    }

    private void _walkSourceFolder(File directory) {
        if (!directory.isDirectory()) {
            return;
        }

        if (isValidRootFolder(directory)) {
            listAccrualFolders(directory);
        } else {
            File[] subFolders = directory.listFiles();
            if (subFolders == null) {
                return;
            }
            for (File subFolder : subFolders) {
                _walkSourceFolder(subFolder);
            }
        }
    }

    private void listAccrualFolders(File directory) {
        File[] subFolders = directory.listFiles();
        if (subFolders == null) {
            return;
        }
        for (File subFolder : subFolders) {
            MetsGenerationHandler generationProcessor = new MetsGenerationHandler(null, directory, subFolder,null);
            _executor_service.submit(generationProcessor);
        }
    }

    public boolean isValidRootFolder(File directory) {
        if (!directory.isDirectory()) {
            return false;
        }

        Matcher matcher = pattern.matcher(directory.getName());
        int matches = 0;
        while (matcher.find()) {
            matches++;
        }
        return matches > 0;
    }
}
