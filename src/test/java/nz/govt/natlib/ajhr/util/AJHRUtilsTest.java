package nz.govt.natlib.ajhr.util;

import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

public class AJHRUtilsTest {

    @Test
    void normalizesTifExtension() {
        String result = AJHRUtils.correctFilename(new File("00100.TIF"), 1, false);
        assertEquals("00100.tif", result);
    }

    @Test
    void leavesNormalFilesUnchangedWhenNoNormalization() {
        String result = AJHRUtils.correctFilename(new File("0003a.tif"), 7, false);
        assertEquals("0003a.tif", result);
    }

    @Test
    void renumbersInflatedFilesSequentially() {
        String result1 = AJHRUtils.correctFilename(new File("00300.tif"), 1, true);
        String result2 = AJHRUtils.correctFilename(new File("00400.tif"), 2, true);
        String result3 = AJHRUtils.correctFilename(new File("00500.tif"), 3, true);

        assertEquals("0001.tif", result1);
        assertEquals("0002.tif", result2);
        assertEquals("0003.tif", result3);
    }

    @Test
    void preservesLetterSuffixes() {
        String result = AJHRUtils.correctFilename(new File("00300a.TIF"), 5, true);
        assertEquals("0005a.tif", result);
    }

    @Test
    void keepsLetterSuffixWhenNoNormalization() {
        String result = AJHRUtils.correctFilename(new File("0003a.tif"), 99, false);
        assertEquals("0003a.tif", result);
    }

    @Test
    void ignoresCounterWhenNormalizationFalse() {
        String result = AJHRUtils.correctFilename(new File("00400.tif"), 42, false);
        assertEquals("00400.tif", result);
    }

    @Test
    void handlesFilesWithoutExtension() {
        String result = AJHRUtils.correctFilename(new File("00300"), 2, true);
        assertEquals("0002", result);
    }

    @Test
    void leavesNonNumericFilesUnchanged() {
        String result = AJHRUtils.correctFilename(new File("foo.tif"), 3, false);
        assertEquals("foo.tif", result);
    }

    @Test
    void handlesAllUppercaseExtension() {
        String result = AJHRUtils.correctFilename(new File("00300A.TIF"), 9, true);
        assertEquals("0009A.tif", result);
    }
}