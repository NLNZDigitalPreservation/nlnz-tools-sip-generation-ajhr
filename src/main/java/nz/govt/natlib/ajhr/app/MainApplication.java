package nz.govt.natlib.ajhr.app;

import nz.govt.natlib.ajhr.proc.MetsFolderScanProcessor;
import nz.govt.natlib.ajhr.util.MultiThreadsPrint;
import nz.govt.natlib.ajhr.util.PrettyPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;
import java.util.Properties;

@SpringBootApplication
@ComponentScan(basePackages = {"nz.govt.natlib.ajhr"})
@EnableAutoConfiguration
public class MainApplication implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(MainApplication.class);

    private final Properties properties = new Properties();
    @Autowired
    private MetsFolderScanProcessor processor;

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        MultiThreadsPrint.init();

        PrettyPrinter.info("Start processing, input arguments:");
        if (args.length == 0 || args[0].equalsIgnoreCase("--h")) {
            printUsage();
            return;
        }
        Arrays.stream(args).forEach(PrettyPrinter::info);
        boolean rstParseArguments = parseArguments(args);
        if (rstParseArguments) {
            PrettyPrinter.info("Succeed to parse arguments");
            processor.setSrcDir(properties.getProperty("srcDir"));
            processor.setDestDir(properties.getProperty("destDir"));
            processor.setForcedReplaced(Boolean.parseBoolean(properties.getProperty("forceReplace")));
            processor.setMaxThreads(Integer.parseInt(properties.getProperty("maxThreads")));
            processor.setStartYear(Integer.parseInt(properties.getProperty("startYear")));
            processor.setEndYear(Integer.parseInt(properties.getProperty("endYear")));
            processor.init();
            processor.walkSourceFolder();
            PrettyPrinter.info(log, "Finished");
            MultiThreadsPrint.close();
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
                PrettyPrinter.error("Invalid arguments: " + key);
                printUsage();
                return false;
            }

            key = key.substring(2); //killed prefix --

            properties.put(key, value);
        }

        if (!properties.containsKey("srcDir") || !properties.containsKey("destDir")) {
            PrettyPrinter.error("Invalid arguments: srcDir or destDir");
            printUsage();
            return false;
        }

        if (!properties.containsKey("forceReplace")) {
            properties.put("forceReplace", "true");
            PrettyPrinter.info("--forceReplace={}", properties.getProperty("forceReplace"));
        } else {
            try {
                Boolean.parseBoolean(properties.getProperty("forceReplace: forceReplace"));
            } catch (Exception e) {
                PrettyPrinter.error("Invalid arguments: forceReplace");
                printUsage();
                return false;
            }
        }

        if (!properties.containsKey("maxThreads")) {
            properties.put("maxThreads", "1");
            PrettyPrinter.info("--maxThreads={}", properties.getProperty("maxThreads"));
        } else {
            try {
                int maxThreads = Integer.parseInt(properties.getProperty("maxThreads"));
                if (maxThreads < 1) {
                    PrettyPrinter.error("Invalid arguments: maxThreads");
                    printUsage();
                    return false;
                }
            } catch (Exception e) {
                PrettyPrinter.error("Invalid arguments: maxThreads");
                printUsage();
                return false;
            }
        }

        if (!properties.containsKey("startYear")) {
            properties.put("startYear", "0");
            PrettyPrinter.info("--startYear={}", properties.getProperty("startYear"));
        } else {
            try {
                int startYear = Integer.parseInt(properties.getProperty("startYear"));
                if (startYear < 0) {
                    PrettyPrinter.error("Invalid arguments: startYear");
                    printUsage();
                    return false;
                }
            } catch (Exception e) {
                PrettyPrinter.error("Invalid arguments: startYear");
                printUsage();
                return false;
            }
        }

        if (!properties.containsKey("endYear")) {
            properties.put("endYear", "0");
            PrettyPrinter.info("--endYear={}", properties.getProperty("endYear"));
        } else {
            try {
                int startYear = Integer.parseInt(properties.getProperty("endYear"));
                if (startYear < 0) {
                    PrettyPrinter.error("Invalid arguments: endYear");
                    printUsage();
                    return false;
                }
            } catch (Exception e) {
                PrettyPrinter.error("Invalid arguments: endYear");
                printUsage();
                return false;
            }
        }

        return true;
    }

    private void printUsage() {
        String msg = "Usage: java -Xms512M -Xmx1024M -jar ajhr.jar [--srcDir=folder] [--destDir=folder] [--forceReplace=true] [--maxThreads=5] [--startYear=0] [--endYear=9999]";
        PrettyPrinter.info(msg);
    }
}
