package nz.govt.natlib.ajhr.proc.redeposit;

import nz.govt.natlib.ajhr.util.MetsUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RedepositIEEndPoint {
    private static final String userDirectory = System.getProperty("user.dir");
    private boolean enable;
    private String srcDir;
    private String destDir;
    private String sheetName;
    private String metsTemplateFilePath;
    private boolean isForcedReplaced;
    private boolean isMultipleRowsExtension;

    public static RedepositIEEndPoint getInstance(String propFileName) throws IOException {
        File fileConfPath = MetsUtils.combinePath(userDirectory, "conf", "resubmission", String.format("application-%s.properties", propFileName.trim()));
        InputStream inputStream = new FileInputStream(fileConfPath);
        //Resource resource = new ClassPathResource();
        Properties properties = new Properties();
        properties.load(inputStream);
        inputStream.close();

        RedepositIEEndPoint instance = new RedepositIEEndPoint();
        instance.setEnable(Boolean.parseBoolean(properties.getProperty("enable")));
        instance.setForcedReplaced(Boolean.parseBoolean(properties.getProperty("isForcedReplaced")));
        instance.setMultipleRowsExtension(Boolean.parseBoolean(properties.getProperty("isMultipleRowsExtension")));
        instance.setSrcDir(properties.getProperty("srcDir"));
        instance.setDestDir(properties.getProperty("destDir"));
        instance.setSheetName(properties.getProperty("sheetName"));

        File fileTemplatePath = MetsUtils.combinePath(userDirectory, "conf", "resubmission", String.format("mets-template-redeposit-%s.xml", propFileName.trim()));
        instance.setMetsTemplateFilePath(fileTemplatePath.getAbsolutePath());

        return instance;
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

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public String getMetsTemplateFilePath() {
        return metsTemplateFilePath;
    }

    public void setMetsTemplateFilePath(String metsTemplateFilePath) {
        this.metsTemplateFilePath = metsTemplateFilePath;
    }

    public boolean isForcedReplaced() {
        return isForcedReplaced;
    }

    public void setForcedReplaced(boolean forcedReplaced) {
        isForcedReplaced = forcedReplaced;
    }

    public boolean isMultipleRowsExtension() {
        return isMultipleRowsExtension;
    }

    public void setMultipleRowsExtension(boolean multipleRowsExtension) {
        isMultipleRowsExtension = multipleRowsExtension;
    }
}
