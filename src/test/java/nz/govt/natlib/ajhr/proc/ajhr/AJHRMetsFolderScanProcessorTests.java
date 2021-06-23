package nz.govt.natlib.ajhr.proc.ajhr;

import nz.govt.natlib.ajhr.proc.MetsTemplateService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;

public class AJHRMetsFolderScanProcessorTests {
    private static final String ROOT_FOLDER = "C:\\Users\\leefr\\workspace\\tmp";
    private static AJHRMetsFolderScanProcessor processor;

    @BeforeAll
    public static void init() {
        String srcDir = new File(ROOT_FOLDER, "AJHR_ORIGINAL").getAbsolutePath();
        String destDir = new File(ROOT_FOLDER, "AJHR_TEST").getAbsolutePath();
        processor = new AJHRMetsFolderScanProcessor(true, srcDir, destDir, 5, true, 0, 9999, new MetsTemplateService());
    }

    @Test
    public void testIsValidFolder() {
        {
            File folder = new File(ROOT_FOLDER, "AJHR_ORIGINAL/AJHR_1861_I_A-G");
            boolean retVal = processor.isValidRootFolder(folder);
            assert retVal;
        }

        {
            File folder = new File(ROOT_FOLDER, "AJHR_ORIGINAL/AJHR_1861_I_A-G/A-01/AC_01");
            boolean retVal = processor.isValidRootFolder(folder);
            assert !retVal;
        }

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
