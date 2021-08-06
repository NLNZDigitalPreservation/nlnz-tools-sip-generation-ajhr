package nz.govt.natlib.ajhr.proc.ajhr;

import nz.govt.natlib.ajhr.util.MetsUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AJHTConfProperties {
    private static final String userDirectory = System.getProperty("user.dir");
    private boolean enable;
    private boolean isForcedReplaced;
    private String srcDir;
    private String destDir;
    private int maxThreads;
    private int startYear;
    private int endYear;

    public static AJHTConfProperties getInstance() throws IOException {
        File fileConfPath = MetsUtils.combinePath(userDirectory, "conf", "ajhr", "ajhr.properties");
        InputStream inputStream = new FileInputStream(fileConfPath);
        //Resource resource = new ClassPathResource();
        Properties properties = new Properties();
        properties.load(inputStream);
        inputStream.close();

        AJHTConfProperties instance = new AJHTConfProperties();

        instance.setEnable(Boolean.parseBoolean(properties.getProperty("enable")));
        instance.setForcedReplaced(Boolean.parseBoolean(properties.getProperty("isForcedReplaced")));
        instance.setSrcDir(properties.getProperty("srcDir"));
        instance.setDestDir(properties.getProperty("destDir"));
        instance.setMaxThreads(Integer.parseInt(properties.getProperty("maxThreads")));
        instance.setStartYear(Integer.parseInt(properties.getProperty("startYear")));
        instance.setEndYear(Integer.parseInt(properties.getProperty("endYear")));

        return instance;
    }

    public static String getUserDirectory() {
        return userDirectory;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
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

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public boolean isForcedReplaced() {
        return isForcedReplaced;
    }

    public void setForcedReplaced(boolean forcedReplaced) {
        isForcedReplaced = forcedReplaced;
    }

    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    public int getEndYear() {
        return endYear;
    }

    public void setEndYear(int endYear) {
        this.endYear = endYear;
    }
}
