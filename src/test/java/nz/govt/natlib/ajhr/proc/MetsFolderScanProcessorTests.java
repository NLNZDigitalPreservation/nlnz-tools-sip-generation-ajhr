package nz.govt.natlib.ajhr.proc;

import nz.govt.natlib.ajhr.metadata.PapersPastTitle;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;

public class MetsFolderScanProcessorTests {
    private static final String ROOT_FOLDER = "/mnt/d/testdata/PP-test";
    private static MetsFolderScanProcessor processor;

    @BeforeAll
    public static void init() {
        processor = new MetsFolderScanProcessor();
        ReflectionTestUtils.setField(processor, "maxThreads", 5);
        ReflectionTestUtils.setField(processor, "srcDir", new File(ROOT_FOLDER, "ALG").getAbsolutePath());
        ReflectionTestUtils.setField(processor, "destDir", new File(ROOT_FOLDER, "test-out").getAbsolutePath());
//        ReflectionTestUtils.setField(processor, "startDate", 19210101);
//        ReflectionTestUtils.setField(processor, "endDate", 19210103);
        ReflectionTestUtils.setField(processor, "spreadsheet", "/mnt/d/testdata/PP-reload-test.xlsx");
        ReflectionTestUtils.setField(processor, "isForcedReplaced", true);
        ReflectionTestUtils.setField(processor, "metsTemplateService", new MetsTemplateService());
        PapersPastTitle papersPastTitle = new PapersPastTitle("ALG", "Albertland Gazette", "9914834303502836",
                "PP10-02", 0, 0);
        ReflectionTestUtils.setField(processor, "papersPastTitle", papersPastTitle);
        processor.init();
    }

    @Test
    public void testIsValidFolder() throws IOException {
        {
            File folder = new File(ROOT_FOLDER, "ALG/1862/ALG_18620614");
            String retVal = processor.getValidSubFolder(folder);
            assert retVal != null;
        }

//        {
//            File folder = new File(ROOT_FOLDER, "AJHR_ORIGINAL/AJHR_1861_I_A-G/A-01/AC_01");
//            boolean retVal = processor.isValidRootFolder(folder);
//            assert !retVal;
//        }

        {
            File folder = FileUtils.getTempDirectory();
            String retVal = processor.getValidSubFolder(folder);
            assert retVal == null;
        }
    }

    @Test
    public void testProcess() throws InterruptedException, IOException {
//        processor.processSpreadsheet();
        assert true;
    }
}
