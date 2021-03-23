package nz.govt.natlib.ajhr.proc;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

public class MetsFolderScanProcessorTests {
    private static MetsFolderScanProcessor processor;

    @BeforeAll
    public static void init() {
        processor = new MetsFolderScanProcessor();
    }

    @Test
    public void testIsValidFolder() {
        {
            File folder = new File("Y:\\paperspast\\objects\\15\\other\\AJHR\\OCR\\AJHR_1858_I_A-G");
            boolean retVal = processor.isValidRootFolder(folder);
            assert retVal;
        }

        {
            File folder = new File("Y:\\paperspast\\objects\\14\\other");
            boolean retVal = processor.isValidRootFolder(folder);
            assert !retVal;
        }
    }
}
