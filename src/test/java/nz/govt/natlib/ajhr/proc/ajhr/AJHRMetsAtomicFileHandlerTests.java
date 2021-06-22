package nz.govt.natlib.ajhr.proc.ajhr;

import nz.govt.natlib.ajhr.proc.ajhr.AJHRMetsAtomicFileHandler;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class AJHRMetsAtomicFileHandlerTests {

    @Test
    public void testDigestOfficial() throws IOException, NoSuchAlgorithmException {
        File f = new File("src/test/resources/image.tif");

        AJHRMetsAtomicFileHandler testInstance = new AJHRMetsAtomicFileHandler(f);

        boolean retVal = testInstance.md5Digest();
        assert retVal;
        String digest = testInstance.getDigestString();
        assert digest != null;
        assert digest.equals("48c0185f9c5568a7912ba9cba03071a8");
    }
}
