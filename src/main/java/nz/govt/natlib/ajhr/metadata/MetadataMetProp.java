package nz.govt.natlib.ajhr.metadata;

import org.apache.commons.lang3.StringUtils;

public class MetadataMetProp {
    private static final String[] TITLE_LIST = {"AJHR"};
    private String title;
    private String year;
    private String volume;
    private String accrualPeriodicity;

    public static MetadataMetProp getInstance(String rootFolderName, String accrualPeriodicity) {
        int idxStart = 0, idxEnd = rootFolderName.indexOf('_', idxStart);
        if (idxEnd < 0) {
            return null;
        }
        String title = rootFolderName.substring(idxStart, idxEnd);
        if (!isValidTitle(title)) {
            return null;
        }

        idxStart = idxEnd + 1;
        idxEnd = rootFolderName.indexOf('_', idxStart);
        if (idxEnd < 0) {
            return null;
        }
        String year = rootFolderName.substring(idxStart, idxEnd);
        if (!isValidYear(year)) {
            return null;
        }

        idxStart = idxEnd + 1;
        String volume = rootFolderName.substring(idxStart);
        if (StringUtils.isEmpty(volume)) {
            return null;
        }

        MetadataMetProp metProp = new MetadataMetProp();
        metProp.setTitle(title);
        metProp.setYear(year);
        metProp.setVolume(volume);
        metProp.setAccrualPeriodicity(accrualPeriodicity);

        return metProp;
    }

    private static boolean isValidTitle(String title) {
        if (StringUtils.isEmpty(title)) {
            return false;
        }
        for (String item : TITLE_LIST) {
            if (item.equalsIgnoreCase(title)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidYear(String year) {
        if (StringUtils.isEmpty(year)) {
            return false;
        }

        return year.length() == 4 && (year.startsWith("18") || year.startsWith("19") || year.startsWith("20"));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getAccrualPeriodicity() {
        return accrualPeriodicity;
    }

    public void setAccrualPeriodicity(String accrualPeriodicity) {
        this.accrualPeriodicity = accrualPeriodicity;
    }
}
