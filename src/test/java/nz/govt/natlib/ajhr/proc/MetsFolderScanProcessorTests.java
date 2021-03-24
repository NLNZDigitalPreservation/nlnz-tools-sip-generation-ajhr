package nz.govt.natlib.ajhr.proc;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;

public class MetsFolderScanProcessorTests {
    private static MetsFolderScanProcessor processor;

    @BeforeAll
    public static void init() {
        processor = new MetsFolderScanProcessor();
        ReflectionTestUtils.setField(processor, "maxThreads", 5);
        ReflectionTestUtils.setField(processor, "sourceFolder", "Y:\\ndha\\pre-deposit_prod\\frank\\AJHR_ORIGINAL");
        ReflectionTestUtils.setField(processor, "targetFolder", "Y:\\ndha\\pre-deposit_prod\\frank\\AJHR_TEST");
        ReflectionTestUtils.setField(processor, "isForcedReloaded", true);
        ReflectionTestUtils.setField(processor, "metsTemplateService", new MetsTemplateService());
        processor.init();
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

        {
            File folder = new File("Y:\\paperspast\\objects\\15\\.history\\2021_02_21_2300");
            boolean retVal = processor.isValidRootFolder(folder);
            assert !retVal;
        }
    }

    @Test
    public void testProcess() throws InterruptedException {
        processor.walkSourceFolder();
        assert true;
    }
}
