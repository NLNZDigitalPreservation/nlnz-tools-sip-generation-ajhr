package nz.govt.natlib.ajhr.util;

import nz.govt.natlib.ajhr.metadata.MetadataRetVal;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PrettyPrinter {
    public static final String OS = SystemUtils.OS_NAME;
    public static final String TERM = System.getenv().get("TERM");
    public static final String IDEAL = System.getenv().get("IDEA_INITIAL_DIRECTORY");


    public static String ANSI_RESET = "\u001B[0m";
    public static String ANSI_BLUE = "\u001B[34m";
    public static String ANSI_RED = "\u001B[31m";
    public static String ANSI_GREEN = "\u001B[32m";
    public static String ANSI_CYAN = "\u001B[36m";

    static {
//        System.getenv().forEach((k, v) -> {
//            println(k + "=" + v);
//        });
//        println("OS: " + OS + " TERM: " + TERM);
        if (OS != null && OS.startsWith("Windows") && TERM == null && IDEAL == null) {
            ANSI_RESET = "";
            ANSI_BLUE = "";
            ANSI_RED = "";
            ANSI_GREEN = "";
            ANSI_CYAN = "";
        }
    }

    public static void printResult(MetadataRetVal retVal, String... args) {
//        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        String msg = getTimestamp(LocalDateTime.now()) + " " + String.format("[%s] %s", retVal.name().toUpperCase(), format(args));
        switch (retVal) {
            case FAIL:
                println(ANSI_RED + msg);
                break;
            case SKIP:
                println(ANSI_CYAN + msg);
                break;
            case SUCC:
                println(ANSI_GREEN + msg);
                break;
            default:
                println(msg);
        }

//        println(ExceptionUtils.getStackTrace(new Exception()));
    }

    public static void debug(String... args) {
        println(ANSI_CYAN + format(args));
    }

    public static void error(String... args) {
        println(ANSI_RED + format(args));
    }

    public static void info(String... args) {
        println(ANSI_RESET + format(args));
    }

    public static void debug(Logger log, String... args) {
        String msg = format(args);
        log.debug(msg);
        println(ANSI_CYAN + msg);
    }

    public static void error(Logger log, String... args) {
        String msg = format(args);
        log.error(msg);
        println(ANSI_RED + msg);
    }

    public static void info(Logger log, String... args) {
        String msg = format(args);
        log.info(msg);
        println(ANSI_RESET + msg);
    }

    public static void debug(Logger log, Exception e, String... args) {
        String msg = format(args);
        log.debug(msg, e);
        println(ANSI_CYAN + msg);
        println(ANSI_CYAN + ExceptionUtils.getStackTrace(e));
    }

    public static void error(Logger log, Exception e, String... args) {
        String msg = format(args);
        log.error(msg, e);
        println(ANSI_RED + msg);
        println(ANSI_RED + ExceptionUtils.getStackTrace(e));
    }

    public static void info(Logger log, Exception e, String... args) {
        String msg = format(args);
        log.info(msg, e);
        println(ANSI_RESET + msg);
        println(ANSI_RESET + ExceptionUtils.getStackTrace(e));
    }

    public static void println(String msg) {
//        System.out.println(msg + ANSI_RESET);
        MultiThreadsPrint.putFinished(msg + ANSI_RESET);
    }

    private static String format(String... args) {
        if (args.length == 0) {
            return "";
        }

        Object[] strItems = new String[args.length - 1];
        for (int i = 0; i < args.length - 1; i++) {
            strItems[i] = args[i + 1];
        }
        String base = args[0].replaceAll("\\{}", "%s");
        String retVal = String.format(base, strItems);

        return retVal;
    }

    private static String getTimestamp(LocalDateTime ldtNow) {
        String timestamp = ldtNow.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        if (timestamp.length() > 23) {
            timestamp = timestamp.substring(0, 23);
        }
        return timestamp;
    }
}
