package nz.govt.natlib.ajhr.metadata;

import nz.govt.natlib.ajhr.util.MetsUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RedepositIeDTO {
    private String originalPID;
    private String dcTitle;
    private String dcType;
    private String dcIdentifier;
    private String userDefinedA;
    private String recordId;
    private String policyId;
    private String ieCreationDate;
    private String preservationType;
    private String hardwareUsed;
    private String physicalCarrierMedia;
    private String multiFileFlag;
    private String numFiles;
    private String fileCreationDate;
    private String fileModificationDate;
    private boolean modifiedMasterFlag;
    private String ieDmdNo;
    private String ieDmdYear;
    private String ieDmdMonth;
    private String ieDmdDay;
    private String almaMms; //For OneoffIE

    private String eventDateTime; //The moment when the SIP is generated

    private List<RedepositIeFileDTO> files = new ArrayList<>();

    public RedepositIeDTO() {
        LocalDateTime ldt = LocalDateTime.now();
        this.eventDateTime = String.format("%s %s %02d %s NZDT %d", ldt.getDayOfWeek().name(), ldt.getMonth().name(), ldt.getDayOfMonth(), ldt.format(DateTimeFormatter.ISO_LOCAL_TIME), ldt.getYear());
    }

    public String getOriginalPID() {
        return originalPID;
    }

    public void setOriginalPID(String originalPID) {
        this.originalPID = originalPID;
    }

    public String getDcTitle() {
        return dcTitle;
    }

    public void setDcTitle(String dcTitle) {
        this.dcTitle = dcTitle;
    }

    public String getDcType() {
        return dcType;
    }

    public void setDcType(String dcType) {
        this.dcType = dcType;
    }

    public String getDcIdentifier() {
        return dcIdentifier;
    }

    public void setDcIdentifier(String dcIdentifier) {
        this.dcIdentifier = dcIdentifier;
    }

    public String getUserDefinedA() {
        return userDefinedA;
    }

    public void setUserDefinedA(String userDefinedA) {
        this.userDefinedA = userDefinedA;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = Long.toString(MetsUtils.parseNumber(recordId));
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = Long.toString(MetsUtils.parseNumber(policyId));
        ;
    }

    public String getIeCreationDate() {
        return ieCreationDate;
    }

    public void setIeCreationDate(String ieCreationDate) {
        if (StringUtils.isEmpty(ieCreationDate)) {
            this.ieCreationDate = "-";
        } else {
            this.ieCreationDate = ieCreationDate;
        }
    }

    public String getPreservationType() {
        return preservationType;
    }

    public void setPreservationType(String preservationType) {
        this.preservationType = preservationType;
    }

    public String getHardwareUsed() {
        return hardwareUsed;
    }

    public void setHardwareUsed(String hardwareUsed) {
        this.hardwareUsed = hardwareUsed;
    }

    public String getPhysicalCarrierMedia() {
        return physicalCarrierMedia;
    }

    public void setPhysicalCarrierMedia(String physicalCarrierMedia) {
        this.physicalCarrierMedia = physicalCarrierMedia;
    }

    public String getMultiFileFlag() {
        return multiFileFlag;
    }

    public void setMultiFileFlag(String multiFileFlag) {
        this.multiFileFlag = multiFileFlag;
    }

    public int getNumFiles() {
        if (StringUtils.isEmpty(numFiles)) {
            return 0;
        } else {
            return Integer.parseInt(numFiles);
        }
    }

    public void setNumFiles(String numFiles) {
        this.numFiles = Long.toString(MetsUtils.parseNumber(numFiles));
    }

    public String getFileCreationDate() {
        return fileCreationDate;
    }

    public void setFileCreationDate(String fileCreationDate) {
        if (StringUtils.isEmpty(fileCreationDate)) {
            this.fileCreationDate = "-";
        } else {
            this.fileCreationDate = fileCreationDate;
        }
    }

    public String getFileModificationDate() {
        return fileModificationDate;
    }

    public void setFileModificationDate(String fileModificationDate) {
        if (StringUtils.isEmpty(fileModificationDate)) {
            this.fileModificationDate = "-";
        } else {
            this.fileModificationDate = fileModificationDate;
        }
    }

    public boolean isModifiedMasterFlag() {
        return modifiedMasterFlag;
    }

    public void setModifiedMasterFlag(boolean modifiedMasterFlag) {
        this.modifiedMasterFlag = modifiedMasterFlag;
    }

    public String getIeDmdNo() {
        return ieDmdNo;
    }

    public void setIeDmdNo(String ieDmdNo) {
        if (StringUtils.isEmpty(ieDmdNo)) {
            this.ieDmdNo = "-";
        } else {
            this.ieDmdNo = ieDmdNo;
        }
    }

    public String getIeDmdYear() {
        if (StringUtils.isEmpty(ieDmdYear)) {
            return "-";
        }
        return ieDmdYear;
    }

    public void setIeDmdYear(String ieDmdYear) {
        this.ieDmdYear = ieDmdYear;
    }

    public String getIeDmdMonth() {
        if (StringUtils.isEmpty(ieDmdMonth)) {
            return "-";
        }
        return ieDmdMonth;
    }

    public void setIeDmdMonth(String ieDmdMonth) {
        this.ieDmdMonth = ieDmdMonth;
    }

    public String getIeDmdDay() {
        if (StringUtils.isEmpty(ieDmdDay)) {
            return "-";
        }
        return ieDmdDay;
    }

    public void setIeDmdDay(String ieDmdDay) {
        this.ieDmdDay = ieDmdDay;
    }

    public String getAlmaMms() {
        return almaMms;
    }

    public void setAlmaMms(String almaMms) {
        this.almaMms = almaMms;
    }

    public List<RedepositIeFileDTO> getFiles() {
        return files;
    }

    public void setFiles(List<RedepositIeFileDTO> files) {
        this.files = files;
    }

    public String getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(String eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public void setValue(String key, Cell cell) {
        DataFormatter formatter = new DataFormatter();
        String strValue = cell == null ? "" : formatter.formatCellValue(cell);

        setValue(key, strValue);
    }

    public void setValue(String key, String value) {
        if (StringUtils.isEmpty(key)) {
            return;
        }

        if (key.equalsIgnoreCase("Original PID")) {
            this.setOriginalPID(value);
        }

        if (key.equalsIgnoreCase("dc:title")) {
            this.setDcTitle(value);
        }
        if (key.equalsIgnoreCase("dc:type")) {
            this.setDcType(value);
        }
        if (key.equalsIgnoreCase("dc:identifier")) {
            this.setDcIdentifier(value);
        }
        if (key.equalsIgnoreCase("UserDefinedA")) {
            this.setUserDefinedA(value);
        }
        if (key.equalsIgnoreCase("recordId")) {
            this.setRecordId(value);
        }
        if (key.equalsIgnoreCase("policyID")) {
            this.setPolicyId(value);
        }
        if (key.equalsIgnoreCase("IE Creation Date")) {
            this.setIeCreationDate(value);
        }
        if (key.equalsIgnoreCase("Preservation Type")) {
            this.setPreservationType(value);
        }
        if (key.equalsIgnoreCase("hardwareUsed")) {
            this.setHardwareUsed(value);
        }
        if (key.equalsIgnoreCase("Physical Carrier Media")) {
            this.setPhysicalCarrierMedia(value);
        }
        if (key.equalsIgnoreCase("Yes /No")) {
            this.setMultiFileFlag(value);
        }
        if (key.equalsIgnoreCase("No Files")) {
            this.setNumFiles(value);
        }
        if (key.equalsIgnoreCase("File Creation Date")) {
            this.setFileCreationDate(value);
        }
        if (key.equalsIgnoreCase("File Modification Date")) {
            this.setFileModificationDate(value);
        }
        if (key.equalsIgnoreCase("Yes / No")) {
            this.setModifiedMasterFlag(value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true"));
        }
        if (key.equalsIgnoreCase("ALMAMMS")) {
            this.setAlmaMms(value);
        }
        if (key.equalsIgnoreCase("no")) {
            this.setIeDmdNo(value);
        }

        if (key.equalsIgnoreCase("year")) {
            this.setIeDmdYear(value);
        }
        if (key.equalsIgnoreCase("month")) {
            this.setIeDmdMonth(value);
        }
        if (key.equalsIgnoreCase("day")) {
            this.setIeDmdDay(value);
        }
    }
}
