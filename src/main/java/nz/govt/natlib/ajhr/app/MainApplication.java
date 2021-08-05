package nz.govt.natlib.ajhr.app;

import nz.govt.natlib.ajhr.proc.ajhr.AJHRMetsFolderScanProcessor;
import nz.govt.natlib.ajhr.proc.redeposit.RedepositIEFolderScanProcessor;
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

import java.util.Properties;

@SpringBootApplication
@ComponentScan(basePackages = {"nz.govt.natlib.ajhr"})
@EnableAutoConfiguration
public class MainApplication implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(MainApplication.class);

    private final Properties properties = new Properties();
    @Autowired
    private AJHRMetsFolderScanProcessor metsGenerationProcessor;
    @Autowired
    private RedepositIEFolderScanProcessor redepositIEFolderScanProcessor;

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        MultiThreadsPrint.init();

        PrettyPrinter.info("Start processing");
        metsGenerationProcessor.walkSourceFolder();

        redepositIEFolderScanProcessor.process();

        PrettyPrinter.info(log, "Finished");
        MultiThreadsPrint.close();
    }

    private void printUsage() {
        String msg = "Usage: java -Xms512M -Xmx1024M -jar ajhr.jar";
        PrettyPrinter.info(msg);
    }
}
