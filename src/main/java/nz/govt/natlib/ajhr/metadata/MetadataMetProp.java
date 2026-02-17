package nz.govt.natlib.ajhr.metadata;

import nz.govt.natlib.ajhr.util.AJHRUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetadataMetProp {
    private static final String[] TITLE_LIST = {"MEX"};
    private String title;
//    private String year;
//    private String month;
//    private String day;
//    private String date;
//    private String mmsId;
    private String IRN;

    private static final Pattern PATTERN =
            Pattern.compile("^[A-Za-z]+(\\d+)(?:_.*)?$");

    public MetadataMetProp() {
    }
//    private String volume;
//    private String accrualPeriodicity;

    public static MetadataMetProp getInstance(String rootFolderName) {
//        int idxStart = 0, idxEnd = rootFolderName.indexOf('_', idxStart);
//        if (idxEnd < 0) {
//            return null;
//        }
//        String title = rootFolderName.substring(idxStart, idxEnd);
//        if (!isValidTitle(title)) {
//            return null;
//        }
//
//        idxStart = idxEnd + 1;
//        idxEnd = rootFolderName.length();
//        if (idxEnd < 0) {
//            return null;
//        }
//        String date = rootFolderName.substring(idxStart, idxEnd);
//        if (!AJHRUtils.isValidDate(date)) {
//            return null;
//        }
//        String year = date.substring(0,4);
//        String month = date.substring(4,6);
//        String day = date.substring(6,8);

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


        String IRN = "";

        Matcher matcher = PATTERN.matcher(rootFolderName);
        if (matcher.matches()) {
            IRN = matcher.group(1);
        }


        MetadataMetProp metProp = new MetadataMetProp();
        metProp.setTitle(rootFolderName);
        metProp.setIRN(IRN);
//        metProp.setDate(date);
//        metProp.setYear(year);
//        metProp.setMonth(month);
//        metProp.setDay(day);
//        metProp.setMmsId(mmsId);

//        metProp.setVolume(volume);
//        metProp.setAccrualPeriodicity(accrualPeriodicity);

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIRN() {
        return IRN;
    }

    public void setIRN(String IRN) {
        this.IRN = IRN;
    }

    //    public String getDate() {
//        return date;
//    }
//
//    public void setDate(String date) {
//        this.date = date;
//    }
//
//    public String getYear() {
//        return year;
//    }
//
//    public void setYear(String year) {
//        this.year = year;
//    }
//
//    public String getMonth() {
//        return month;
//    }
//
//    public void setMonth(String month) {
//        this.month = month;
//    }
//
//    public String getDay() {
//        return day;
//    }
//
//    public void setDay(String day) {
//        this.day = day;
//    }
//
//    public String getMmsId() {
//        return mmsId;
//    }
//
//    public void setMmsId(String mmsId) {
//        this.mmsId = mmsId;
//    }
}
