package nz.govt.natlib.ajhr.util;

public class LogPrinter {
    public static void error(String msg) {
        System.out.println("[ERROR] " + msg);
    }

    public static void info(String msg) {
        System.out.println("[INFO] " + msg);
    }
}
