package nz.govt.natlib.ajhr.util;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class MetsUtils {
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

    public static long parseNumber(String strVal) {
        if (StringUtils.isEmpty(strVal)) {
            return 0;
        }
        try {
            return (long) Double.parseDouble(strVal.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
