package nz.govt.natlib.ajhr.metadata;

import nz.govt.natlib.ajhr.util.AJHRUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.time.Duration;
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
    private String identifier;

    private static final Pattern PATTERN =
            Pattern.compile("^[A-Za-z]+(\\d+)(?:_.*)?$");

    public MetadataMetProp() {
    }
//    private String volume;
//    private String accrualPeriodicity;

    public static MetadataMetProp getInstance(String rootFolderName) throws IOException, URISyntaxException, ParserConfigurationException, SAXException {
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
//        metProp.setTitle(rootFolderName);
        metProp.setIRN(IRN);
//        metProp.setDate(date);
//        metProp.setYear(year);
//        metProp.setMonth(month);
//        metProp.setDay(day);
//        metProp.setMmsId(mmsId);

//        metProp.setVolume(volume);
//        metProp.setAccrualPeriodicity(accrualPeriodicity);
        sruSearch(IRN, metProp);

        return metProp;
    }

    private static void sruSearch(String IRN, MetadataMetProp metProp) throws IOException, URISyntaxException, ParserConfigurationException, SAXException {

        String sruURL = String.format("http://wlgortemudbapp02.natlib.govt.nz:8080/sru?operation=searchRetrieve&recordPacking=xml&recordSchema=dps&query=IRN=" + IRN);
        URL url = new URL(sruURL);
        String response = IOUtils.toString(url.toURI(), Charset.defaultCharset());
        System.out.println(response);

        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(response)));


        NodeList titles = doc.getElementsByTagName("dc:title");
        for (int i = 0; i < titles.getLength(); i++) {
            String text = titles.item(i).getTextContent();
            if (text != null && !text.isBlank()) {
                metProp.setTitle(text);
            }
        }

        NodeList identifiers = doc.getElementsByTagName("dc:identifier");
        for (int i = 0; i < identifiers.getLength(); i++) {
            String text = identifiers.item(i).getTextContent();
            if (text != null && !text.isBlank() && text.startsWith("REF:")) {
                metProp.setIdentifier(text);
            }
        }



//        HttpClient client = HttpClient.newBuilder()
//                .connectTimeout(Duration.ofSeconds(10))
//                .build();
//
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("http://wlgortemudbapp02.natlib.govt.nz:8080/sru?operation=searchRetrieve&recordPacking=xml&recordSchema=dps&query=IRN=" + IRN))
//                .header("Accept", "application/xml")
////                .timeout(Duration.ofSeconds(20))
//                .GET()
//                .build();
//
//        HttpResponse<String> response =
//                client.send(request, HttpResponse.BodyHandlers.ofString());
//
//        if (response.statusCode() == 200) {
//            String xml = response.body();
//            System.out.println("Fetched XML:\n" + xml);
//        } else {
//            System.err.println("HTTP error: " + response.statusCode());
//        }

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

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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
