package nz.govt.natlib.ajhr.metadata;

import org.apache.commons.lang3.StringUtils;

public class RedepositIeFileDTO {
    private String originalPID;
    private String fileName;
    private String fileCreationDate;
    private String fileModificationDate;

    public String getOriginalPID() {
        return originalPID;
    }

    public void setOriginalPID(String originalPID) {
        this.originalPID = originalPID;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileCreationDate() {
        return fileCreationDate;
    }

    public void setFileCreationDate(String fileCreationDate) {
        this.fileCreationDate = fileCreationDate;
    }

    public String getFileModificationDate() {
        return fileModificationDate;
    }

    public void setFileModificationDate(String fileModificationDate) {
        this.fileModificationDate = fileModificationDate;
    }

    public void setValue(String key, String value) {
        if (StringUtils.isEmpty(key)) {
            return;
        }

        if (key.equalsIgnoreCase("Original PID")) {
            this.setOriginalPID(value);
        }

        if (key.equalsIgnoreCase("Filename")) {
            this.setFileName(value);
        }
        if (key.equalsIgnoreCase("File Creation Date")) {
            this.setFileCreationDate(value);
        }
        if (key.equalsIgnoreCase("File Modification Date")) {
            this.setFileModificationDate(value);
        }
    }
}
