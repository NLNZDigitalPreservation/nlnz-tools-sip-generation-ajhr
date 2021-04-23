package nz.govt.natlib.ajhr.app;

import nz.govt.natlib.ajhr.proc.MetsFolderScanProcessor;
import nz.govt.natlib.ajhr.util.PrettyPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;
import java.util.Properties;

@SpringBootApplication
@ComponentScan(basePackages = {"nz.govt.natlib.ajhr"})
public class MainApplication implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(MainApplication.class);

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_CYAN = "\u001B[36m";

    private final Properties properties = new Properties();
    @Autowired
    private MetsFolderScanProcessor processor;

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        PrettyPrinter.info("Start processing, input arguments:");
        Arrays.stream(args).forEach(PrettyPrinter::info);
        boolean rstParseArguments = parseArguments(args);
        if (rstParseArguments) {
            PrettyPrinter.info("Succeed to parse arguments");
            processor.setSrcDir(properties.getProperty("srcDir"));
            processor.setDestDir(properties.getProperty("destDir"));
            processor.setForcedReplaced(Boolean.parseBoolean(properties.getProperty("forceReplace")));
            processor.setMaxThreads(Integer.parseInt(properties.getProperty("maxThreads")));
            processor.init();
            processor.walkSourceFolder();
        }
    }

    private boolean parseArguments(String... args) {
        for (String arg : args) {
            String[] items = arg.split("=");
            if (items.length != 2) {
                PrettyPrinter.error("Invalid arguments");
                printUsage();
                return false;
            }

            String key = items[0].trim(), value = items[1].trim();
            if (!key.startsWith("--")) {
                PrettyPrinter.error("Invalid arguments");
                printUsage();
                return false;
            }

            key = key.substring(2); //killed prefix --

            properties.put(key, value);
        }

        if (!properties.containsKey("srcDir") || !properties.containsKey("destDir")) {
            PrettyPrinter.error("Invalid arguments");
            printUsage();
            return false;
        }

        if (!properties.containsKey("forceReplace")) {
            PrettyPrinter.info("The flag 'forceReplace' is set to 'true");
            properties.put("forceReplace", true);
        } else {
            try {
                Boolean.parseBoolean(properties.getProperty("forceReplace"));
            } catch (Exception e) {
                PrettyPrinter.error("Invalid arguments");
                printUsage();
                return false;
            }
        }

        if (!properties.containsKey("maxThreads")) {
            PrettyPrinter.info("The 'maxThreads' is set to '1");
            properties.put("maxThreads", "1");
        } else {
            try {
                int maxThreads = Integer.parseInt(properties.getProperty("maxThreads"));
                if (maxThreads <= 1) {
                    PrettyPrinter.error("Invalid arguments");
                    printUsage();
                    return false;
                }
            } catch (Exception e) {
                PrettyPrinter.error("Invalid arguments");
                printUsage();
                return false;
            }
        }

        return true;
    }

    private void printUsage() {
        String msg = "Usage: java -Xms512M -Xmx1024M -jar ajhr.jar [--srcDir=folder] [--destDir=folder] [--forceReplace=true] [--maxThreads=5]";
        PrettyPrinter.info(msg);
    }
}
