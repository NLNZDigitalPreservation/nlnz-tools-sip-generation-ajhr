package nz.govt.natlib.ajhr.proc.redeposit;

import freemarker.template.Template;
import nz.govt.natlib.ajhr.metadata.MetadataRetVal;
import nz.govt.natlib.ajhr.metadata.RedepositIeDTO;
import nz.govt.natlib.ajhr.util.MetsUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;

public class RedepositIEMetsGenerationHandlerTests {
    private static final String currentDir = System.getProperty("user.dir");
    private static final File redepositDir = MetsUtils.combinePath(currentDir, "src", "test", "data", "redeposit");
    private static final File rootDestDir = MetsUtils.combinePath(FileUtils.getTempDirectory(), "redeposit");

    private static Template template;

    @BeforeAll
    public static void init() {
        template = mock(Template.class);
    }

    @Test
    public void testOneOffIEHandler() throws IOException {
        RedepositIeDTO dto = new RedepositIeDTO();
        dto.setOriginalPID("IE25602831");
        dto.setNumFiles("2");
        File srcDir = MetsUtils.combinePath(redepositDir, "OneOffIE", dto.getOriginalPID());
        File destDir = MetsUtils.combinePath(rootDestDir, "OneOffIE", dto.getOriginalPID());

        { //Test normal case
            RedepositIEMetsGenerationHandler handler = new RedepositIEMetsGenerationHandler(template, srcDir.getAbsolutePath(), destDir.getAbsolutePath(), dto, true, false);
            MetadataRetVal retVal = handler.process();
            assert retVal == MetadataRetVal.SUCC;
        }

        {
            //Test force replace case
            RedepositIEMetsGenerationHandler handler = new RedepositIEMetsGenerationHandler(template, srcDir.getAbsolutePath(), destDir.getAbsolutePath(), dto, true, false);
            MetadataRetVal retVal = handler.process();
            assert retVal == MetadataRetVal.SUCC;
        }

        {
            //Test skip  case
            RedepositIEMetsGenerationHandler handler = new RedepositIEMetsGenerationHandler(template, srcDir.getAbsolutePath(), destDir.getAbsolutePath(), dto, false, false);
            MetadataRetVal retVal = handler.process();
            assert retVal == MetadataRetVal.SKIP;
        }

        {
            //Test failed case
            dto.setNumFiles("1");
            RedepositIEMetsGenerationHandler handler = new RedepositIEMetsGenerationHandler(template, srcDir.getAbsolutePath(), destDir.getAbsolutePath(), dto, true, false);
            MetadataRetVal retVal = MetadataRetVal.SUCC;

            try {
                retVal = handler.process();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            assert retVal == MetadataRetVal.FAIL;
        }
    }
}
