package nz.govt.natlib.ajhr.proc;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import nz.govt.natlib.ajhr.metadata.MetadataMetProp;
import nz.govt.natlib.ajhr.metadata.MetadataSipItem;
import nz.govt.natlib.ajhr.util.AJHRUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class MetsGenerationHandlerTests {
    private static final String ROOT_FOLDER = "/home/jeremy/workspace/testdata";
    private static MetsGenerationHandler testInstance;

    @BeforeAll
    public static void init() throws IOException {
        MetsTemplateService metsTemplateService = new MetsTemplateService();
        Template template = metsTemplateService.loadTemplate();

        File rootFolder = new File(ROOT_FOLDER, "TDN/1955/TDN_19550113");
        File subFolder = new File(ROOT_FOLDER, "TDN/1955/TDN_19550113/PM_01");
        String targetFolder = new File(ROOT_FOLDER, "TDN_TEST").getAbsolutePath();
        testInstance = new MetsGenerationHandler(template, rootFolder, subFolder, targetFolder, false);
    }

    @Test
    public void testProcess() throws IOException, TemplateException, NoSuchAlgorithmException {
        testInstance.process();

        File targetStreamFolder = AJHRUtils.combinePath(testInstance.getTargetRootLocation(), "content", "streams");
        assert targetStreamFolder.exists();
        assert targetStreamFolder.isDirectory();
    }

    @Test
    public void testCreateMetsXml() throws IOException, TemplateException, NoSuchAlgorithmException {
        String metsXml = testInstance.createMetsXmlAndCopyStreams();
        System.out.println(metsXml);
        assert metsXml != null;
        assert metsXml.length() > 0;
    }

//    @Test
//    public void testParseMetProp() {
//        MetadataMetProp metProp = MetadataMetProp.getInstance(testInstance.getRootDirectory().getName(), testInstance.getSubFolder().getName());
//        assert metProp != null;
//        assert metProp.getTitle().equals("TDN");
//        assert metProp.getDate().equals("19550113");
//        assert metProp.getYear().equals("1955");
//        assert metProp.getMonth().equals("01");
//        assert metProp.getDay().equals("13");
//        assert metProp.getMmsId().equals("9916300343502836");
//    }

//    @Test
//    public void testHandleFiles() throws IOException, NoSuchAlgorithmException {
//        File folder = new File(ROOT_FOLDER, "TDN/1955/TDN_19550113/PM_01");
//        List<MetadataSipItem> list = testInstance.handleFiles(null, folder, FileUtils.getTempDirectory());
//        assert list != null;
//        assert list.size() > 0;
//
//        list.forEach(System.out::println);
//
//        MetadataSipItem item = list.get(0);
//        assert item.getFileId() == 1;
//        assert item.getFixityValue().length() > 0;
//    }

//    @Test
//    public void testGetFileEntityTypeFromExt() {
//        {
//            String fileName = "mets.XML";
//            String entityType = testInstance.getFileEntityTypeFromExt(fileName);
//            assert entityType.equals("METS");
//        }
//
//        {
//            String fileName = "my_mets.xml";
//            String entityType = testInstance.getFileEntityTypeFromExt(fileName);
//            assert entityType.equals("METS");
//        }
//
//        {
//            String fileName = "mets_test.XML";
//            String entityType = testInstance.getFileEntityTypeFromExt(fileName);
//            assert entityType.equals("ALTO");
//        }
//
//        {
//            String fileName = "test.tif";
//            String entityType = testInstance.getFileEntityTypeFromExt(fileName);
//            assert entityType.equals("TIFF");
//        }
//    }

//    @Test
//    public void testDigest() throws IOException {
//        File f = new File(ROOT_FOLDER, "TDN/1955/TDN_19550113/PM_01/0001.tif");
//        String fixityValue = testInstance.digest(f);
//        assert fixityValue != null;
//        assert fixityValue.length() > 0;
//    }
//
//    @Test
//    public void testDigestOfficial() {
//        File f = new File("src/test/resources/image.tif");
//        String digest = testInstance.digest(f);
//        assert digest != null;
//        assert digest.equals("48c0185f9c5568a7912ba9cba03071a8");
//    }
}
