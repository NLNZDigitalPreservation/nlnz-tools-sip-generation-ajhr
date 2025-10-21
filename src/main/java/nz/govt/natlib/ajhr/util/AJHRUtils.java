package nz.govt.natlib.ajhr.util;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AJHRUtils {
    public static File combinePath(String root, String... arguments) {
        StringBuilder buf = new StringBuilder(root);
        for (String s : arguments) {
            buf.append(File.separator).append(s);
        }
        return new File(buf.toString());
    }

    public static File combinePath(File root, String... arguments) {
        return combinePath(root.getAbsolutePath(), arguments);
    }

    public static boolean isValidDate(String date) {
        if (StringUtils.isEmpty(date)) {
            return false;
        }
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyymmdd");
        try {
            dateFormatter.parse(date);
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }

    public static String removeExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot == -1) ? filename : filename.substring(0, dot);
    }

    public static String correctFilename(File f, int counter, boolean needsNormalization) {
        String fileName = f.getName();

        if (needsNormalization) {
//            // Extract numeric part and any letter suffix
            String numericPart = fileName.replaceAll("^(\\d+).*", "$1");
            String letterPart = fileName.substring(numericPart.length());
//
//            int num = Integer.parseInt(numericPart);
//
//            // Detect "extra zeros" style like 00100, 00200, 00300
//            // Rule: if numericPart ends with "00" and its length > 4, divide by 100
//            if (numericPart.endsWith("00") && numericPart.length() > 4) {
//                num = num / 100;
//            }
//
//            // Format as 4 digits
//            fileName = String.format("%04d", num) + letterPart;

            // Generate new sequential name
            fileName = String.format("%04d", counter++) + letterPart;
        }

        // Normalize extension
        if (fileName.endsWith(".TIF")) {
            fileName = fileName.substring(0, fileName.lastIndexOf('.')) + ".tif";
        }

        return fileName;
    }
}
