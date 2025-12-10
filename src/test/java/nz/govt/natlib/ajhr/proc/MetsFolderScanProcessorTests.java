package nz.govt.natlib.ajhr.proc;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;

public class MetsFolderScanProcessorTests {
    private static final String ROOT_FOLDER = "/mnt/d/testdata";
    private static MetsFolderScanProcessor processor;

    @BeforeAll
    public static void init() {
        processor = new MetsFolderScanProcessor();
        ReflectionTestUtils.setField(processor, "maxThreads", 5);
        ReflectionTestUtils.setField(processor, "srcDir", new File(ROOT_FOLDER, "MEX").getAbsolutePath());
        ReflectionTestUtils.setField(processor, "destDir", new File(ROOT_FOLDER, "MEX_TEST").getAbsolutePath());
        ReflectionTestUtils.setField(processor, "startDate", 19210101);
        ReflectionTestUtils.setField(processor, "endDate", 19210103);
        ReflectionTestUtils.setField(processor, "isForcedReplaced", true);
        ReflectionTestUtils.setField(processor, "metsTemplateService", new MetsTemplateService());
        processor.init();
    }

    @Test
    public void testIsValidFolder() {
        {
            File folder = new File(ROOT_FOLDER, "MEX/1921/MEX_19210103");
            boolean retVal = processor.isValidRootFolder(folder);
            assert retVal;
        }

//        {
//            File folder = new File(ROOT_FOLDER, "AJHR_ORIGINAL/AJHR_1861_I_A-G/A-01/AC_01");
//            boolean retVal = processor.isValidRootFolder(folder);
//            assert !retVal;
//        }

        {
            File folder = FileUtils.getTempDirectory();
            boolean retVal = processor.isValidRootFolder(folder);
            assert !retVal;
        }
    }

    @Test
    public void testProcess() throws InterruptedException, IOException {
        processor.walkSourceFolder();
        assert true;
    }
}
