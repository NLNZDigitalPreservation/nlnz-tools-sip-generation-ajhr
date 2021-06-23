package nz.govt.natlib.ajhr.proc.redeposit;

public class RedepositIEEndPoint {
    private boolean enable;
    private String srcDir;
    private String destDir;
    private String sheetName;
    private String metsTemplateFileName;
    private boolean isForcedReplaced;
    private boolean isMultipleRowsExtension;

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

    public String getMetsTemplateFileName() {
        return metsTemplateFileName;
    }

    public void setMetsTemplateFileName(String metsTemplateFileName) {
        this.metsTemplateFileName = metsTemplateFileName;
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
