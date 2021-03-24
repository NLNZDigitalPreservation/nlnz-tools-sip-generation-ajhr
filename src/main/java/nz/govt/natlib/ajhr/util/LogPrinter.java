package nz.govt.natlib.ajhr.util;

public class LogPrinter {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_CYAN = "\u001B[36m";

    public static LogLevel level = LogLevel.INFO;

    public static void debug(String msg) {
        if (level.ordinal() >= LogLevel.DEBUG.ordinal()) {
            System.out.println(ANSI_CYAN + "[DEBUG] " + msg);
        }
    }

    public static void error(String msg) {
        if (level.ordinal() >= LogLevel.ERROR.ordinal()) {
            System.out.println(ANSI_RED + "[ERROR] " + msg);
        }
    }

    public static void info(String msg) {
        if (level.ordinal() >= LogLevel.INFO.ordinal()) {
            System.out.println(ANSI_BLUE + "[INFO] " + msg);
        }
    }

    public static enum LogLevel {
        ERROR,
        INFO,
        DEBUG,
    }
}
