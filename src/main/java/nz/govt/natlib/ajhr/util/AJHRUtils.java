package nz.govt.natlib.ajhr.util;

import java.io.File;

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
}
