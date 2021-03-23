package nz.govt.natlib.ajhr.proc;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import nz.govt.natlib.ajhr.metadata.MetadataMetProp;
import nz.govt.natlib.ajhr.metadata.MetadataSipItem;
import nz.govt.natlib.ajhr.util.AJHRUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MetsGenerationHandlerTests {
    private static MetsGenerationHandler testInstance;

    @BeforeAll
    public static void init() throws IOException {
        MetsTemplateService metsTemplateService = new MetsTemplateService();
        Template template = metsTemplateService.loadTemplate();

        File rootFolder = new File("Y:\\ndha\\pre-deposit_prod\\Crust2\\AJHR\\OCR\\AJHR_1858_I_A-G");
        File subFolder = new File("Y:\\ndha\\pre-deposit_prod\\Crust2\\AJHR\\OCR\\AJHR_1858_I_A-G\\A-01");
        String targetFolder = "Y:\\ndha\\pre-deposit_prod\\frank\\AJHR_TEST";
        testInstance = new MetsGenerationHandler(template, rootFolder, subFolder, targetFolder);
    }

    @Test
    public void testProcess() throws IOException, TemplateException {
        testInstance.process();

        File targetStreamFolder = AJHRUtils.combinePath(testInstance.getTargetRootLocation(), "content", "streams");
        assert targetStreamFolder.exists();
        assert targetStreamFolder.isDirectory();
    }

    @Test
    public void testCreateMetsXml() throws IOException, TemplateException {
        String metsXml = testInstance.createMetsXml();
        System.out.println(metsXml);
        assert metsXml != null;
        assert metsXml.length() > 0;
    }

    @Test
    public void testParseMetProp() {
        MetadataMetProp metProp = testInstance.parseMetProp();
        assert metProp != null;
        assert metProp.getTitle().equals("AJHR");
        assert metProp.getYear().equals("1858");
        assert metProp.getVolume().equals("I_A-G");
        assert metProp.getAccrualPeriodicity().equals("A-01");
    }

    @Test
    public void testHandleFiles() throws IOException {
        File folder = new File("Y:\\ndha\\pre-deposit_prod\\Crust2\\AJHR\\OCR\\AJHR_1858_I_A-G\\A-01\\MM_01");
        List<MetadataSipItem> list = testInstance.handleFiles(folder);
        assert list != null;
        assert list.size() > 0;

        list.forEach(System.out::println);

        MetadataSipItem item = list.get(0);
        assert item.getFileId() == 1;
        assert item.getFixityValue().length() > 0;
    }

    @Test
    public void testGetFileEntityTypeFromExt() {
        {
            String fileName = "mets.XML";
            String entityType = testInstance.getFileEntityTypeFromExt(fileName);
            assert entityType.equals("METS");
        }

        {
            String fileName = "my_mets.xml";
            String entityType = testInstance.getFileEntityTypeFromExt(fileName);
            assert entityType.equals("METS");
        }

        {
            String fileName = "mets_test.XML";
            String entityType = testInstance.getFileEntityTypeFromExt(fileName);
            assert entityType.equals("ALTO");
        }

        {
            String fileName = "test.tif";
            String entityType = testInstance.getFileEntityTypeFromExt(fileName);
            assert entityType.equals("TIFF");
        }
    }

    @Test
    public void testDigest() throws IOException {
        File f = new File("Y:\\ndha\\pre-deposit_prod\\Crust2\\AJHR\\OCR\\AJHR_1858_I_A-G\\A-01\\MM_01\\A-01_0001.xml");
        String fixityValue = testInstance.digest(f);
        assert fixityValue != null;
        assert fixityValue.length() > 0;
    }
}
