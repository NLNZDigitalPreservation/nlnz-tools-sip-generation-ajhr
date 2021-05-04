package nz.govt.natlib.ajhr.metadata;

import java.io.File;

public class MetadataSipItem {
    private File file;

    private int fileId;
    private String fileOriginalName;
    private String fileEntityType;
    private String fileSize;

    private String agent = "JDK Message Digest Utility";
    private String fixityValue;
    private String fixityType = "MD5";

    private String label;

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("file: ").append(this.file.getAbsolutePath()).append(System.lineSeparator());
        buf.append("fileId: ").append(this.fileId).append(System.lineSeparator());
        buf.append("fileOriginalName: ").append(this.fileOriginalName).append(System.lineSeparator());
        buf.append("fileEntityType: ").append(this.fileEntityType).append(System.lineSeparator());
        buf.append("fileSize: ").append(this.fileSize).append(System.lineSeparator());
        buf.append("agent: ").append(this.agent).append(System.lineSeparator());
        buf.append("fixityValue: ").append(this.fixityValue).append(System.lineSeparator());
        buf.append("fixityType: ").append(this.fixityType).append(System.lineSeparator());
        return buf.toString();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public String getFileOriginalName() {
        return fileOriginalName;
    }

    public void setFileOriginalName(String fileOriginalName) {
        this.fileOriginalName = fileOriginalName;
    }

    public String getFileEntityType() {
        return fileEntityType;
    }

    public void setFileEntityType(String fileEntityType) {
        this.fileEntityType = fileEntityType;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getFixityValue() {
        return fixityValue;
    }

    public void setFixityValue(String fixityValue) {
        this.fixityValue = fixityValue;
    }

    public String getFixityType() {
        return fixityType;
    }

    public void setFixityType(String fixityType) {
        this.fixityType = fixityType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label.toLowerCase();
    }
}
