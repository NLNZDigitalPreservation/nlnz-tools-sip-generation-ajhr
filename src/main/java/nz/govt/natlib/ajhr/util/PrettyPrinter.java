package nz.govt.natlib.ajhr.util;

import nz.govt.natlib.ajhr.metadata.MetadataRetVal;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PrettyPrinter {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_CYAN = "\u001B[36m";

    public static void printResult(MetadataRetVal retVal, String... args) {
        String msg = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " " + String.format("[%s] %s", retVal.name().toUpperCase(), format(args));
        switch (retVal) {
            case FAIL:
                println(ANSI_RED + msg);
                break;
            case SKIP:
                println(ANSI_CYAN + msg);
            case SUCC:
                println(ANSI_GREEN + msg);
                break;
            default:
                println(msg);
        }
    }

    public static void debug(String... args) {
        println(ANSI_CYAN + format(args));
    }

    public static void error(String... args) {
        println(ANSI_RED + format(args));
    }

    public static void info(String... args) {
        println(ANSI_BLUE + format(args));
    }

    public static void println(String msg) {
        System.out.println(msg + ANSI_RESET);
    }

    private static String format(String... args) {
        if (args.length == 0) {
            return "";
        }

        String base = args[0];
        for (int i = 1; i < args.length; i++) {
            base = base.replaceFirst("\\{\\}", args[i]);
        }
        return base;
    }
}
