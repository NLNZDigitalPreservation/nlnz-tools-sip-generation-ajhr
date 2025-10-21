package nz.govt.natlib.ajhr.metadata;

import nz.govt.natlib.ajhr.util.AJHRUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class MetadataMetProp {
    private static final String[] TITLE_LIST = {"GBARG", "ESD", "NCGAZ", "WAIKIN"};

    private String titleCode;
    private String title;
    private String year;
    private String month;
    private String day;
    private String date;
    private String mmsId;
    private String subReason;
//    private PapersPastTitle papersPastTitle;

    public MetadataMetProp() {
    }
//    private String volume;
//    private String accrualPeriodicity;

//    private static PapersPastTitle createTitle(String titleCode) {
//        return switch (titleCode) {
//            case "GBARG" -> new PapersPastTitle("Golden Bay Argus", "9918168572902836", "PP17-03");
//            case "ESD" -> new PapersPastTitle("The Evening Star", "9917181393502836", "PP17-02");
//            case "LT" -> new PapersPastTitle("Lyttelton Times", "9914701773502836", "PP17-03");
//            case "NCGAZ" -> new PapersPastTitle("North Canterbury Gazette", "9918168573102836", "PP17-03");
//            case "WAIKIN" -> new PapersPastTitle("Waikato Independent", "9917578593502836", "PP17-03");
//            case "NZMAIL" -> new PapersPastTitle("New Zealand Mail", "9918183973502836", "PP17-03");
//
//
//            default -> null;
//        };
//    }

    public static MetadataMetProp getInstance(String rootFolderName, PapersPastTitle ppTitle)  {
        int idxStart = 0, idxEnd = rootFolderName.indexOf('_', idxStart);
        if (idxEnd < 0) {
            return null;
        }
        String titleCode = rootFolderName.substring(idxStart, idxEnd);
//        if (!isValidTitle(titleCode)) {
//            return null;
//        }

//        PapersPastTitle ppTitle = createTitle(titleCode);
//        if (ppTitle == null) {
//            return null;
//        }

//        String title = TITLES.get(titleCode);
//        if (title == null) {
//            return null;
//        }

        idxStart = idxEnd + 1;
        idxEnd = rootFolderName.length();
        if (idxEnd < 0) {
            return null;
        }
        String date = rootFolderName.substring(idxStart, idxEnd);
        if (!AJHRUtils.isValidDate(date)) {
            return null;
        }
        String year = date.substring(0,4);
        String month = date.substring(4,6);
        String day = date.substring(6,8);

//        idxStart = idxEnd + 1;
//        String volume = rootFolderName.substring(idxStart);
//        if (StringUtils.isEmpty(volume)) {
//            return null;
//        }
//        String mmsId = "9914705253502836" ;
//        if (Integer.parseInt(date) < 19600620) {
//            mmsId = "9916300343502836";
//        } else {
//            mmsId = "9919246535602836";
//        }

        MetadataMetProp metProp = new MetadataMetProp();
        metProp.setTitleCode(titleCode);
        metProp.setTitle(ppTitle.title());
        metProp.setDate(date);
        metProp.setYear(year);
        metProp.setMonth(month);
        metProp.setDay(day);
        metProp.setMmsId(ppTitle.MMSID());
        metProp.setSubReason(ppTitle.submissionReason());
//        metProp.setVolume(volume);
//        metProp.setAccrualPeriodicity(accrualPeriodicity);

        return metProp;
    }

    private static boolean isValidTitle(String title) {
        if (StringUtils.isEmpty(title)) {
            return false;
        }
        return Arrays.asList(TITLE_LIST).contains(title);
    }

    public String getTitleCode() {
        return titleCode;
    }

    public void setTitleCode(String titleCode) {
        this.titleCode = titleCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getMmsId() {
        return mmsId;
    }

    public void setMmsId(String mmsId) {
        this.mmsId = mmsId;
    }

    public String getSubReason() {
        return subReason;
    }

    public void setSubReason(String subReason) {
        this.subReason = subReason;
    }
}
