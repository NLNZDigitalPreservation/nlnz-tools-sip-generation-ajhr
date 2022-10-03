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
}
