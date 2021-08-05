package nz.govt.natlib.ajhr.proc;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import nz.govt.natlib.ajhr.metadata.MetadataRetVal;
import nz.govt.natlib.ajhr.util.MetsUtils;
import nz.govt.natlib.ajhr.util.PrettyPrinter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class AbstractMetsGenerationHandler {
    public static final Logger log = LoggerFactory.getLogger(AbstractMetsGenerationHandler.class);

    public static final String PRESERVATION_MASTER_FOLDER = "PM_01";
    public static final String MODIFIED_MASTER_FOLDER = "MM_01";
    public static final String READY_FOR_INGESTION_MARK = "ready-for-ingestion-FOLDER-COMPLETED";
    public static final String STREAM_FOLDER = "content" + File.separator + "streams";
    public static final String PRESERVATION_MASTER_STREAM_FOLDER = STREAM_FOLDER + File.separator + PRESERVATION_MASTER_FOLDER;
    public static final String MODIFIED_MASTER_STREAM_FOLDER = STREAM_FOLDER + File.separator + MODIFIED_MASTER_FOLDER;

    private static DigestUtils _digester = null;

    static {
        try {
            _digester = new DigestUtils(MessageDigest.getInstance("MD5"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    protected Template metTemplate;
    protected File srcFolder;
    protected File destFolder;
    protected boolean isForcedReplaced = false;

    abstract protected String createMetsXmlAndCopyStreams() throws IOException, TemplateException, NoSuchAlgorithmException;

    public MetadataRetVal process() throws IOException {
        File readyForIngestionMarkFile = MetsUtils.combinePath(this.destFolder, READY_FOR_INGESTION_MARK);
        if (readyForIngestionMarkFile.exists() && !isForcedReplaced) {
            log.info("Skip {}", srcFolder.getAbsolutePath());
            return MetadataRetVal.SKIP;
        }

        //Clean the existing target location
        if (this.destFolder.exists()) {
            log.debug("Clear the existing folder: {}", this.destFolder.getAbsolutePath());
            FileUtils.deleteDirectory(this.destFolder);
        }

        String metsXml;
        try {
            metsXml = createMetsXmlAndCopyStreams();
        } catch (IOException | TemplateException | NoSuchAlgorithmException e) {
//            log.error("Failed to generate metsXml and copy files.", e);
            PrettyPrinter.error(log, e, "Failed to generate metsXml and copy files.");
            return MetadataRetVal.FAIL;
        }

        //Write mets xml
        File targetMetsXmlFile = MetsUtils.combinePath(this.destFolder, "content", "mets.xml");
        FileUtils.writeStringToFile(targetMetsXmlFile, metsXml, StandardCharsets.UTF_8);

        //Write ready file to sip folder
        FileUtils.writeByteArrayToFile(readyForIngestionMarkFile, new byte[0]);

        return MetadataRetVal.SUCC;
    }

    public boolean copyDirectory(File sourceDirectory, File targetDirectory) {
        if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
            log.error("The source directory does not exist: {}", sourceDirectory.getAbsolutePath());
            return false;
        }

        if (targetDirectory.exists() && !targetDirectory.isDirectory()) {
            log.error("The target directory is not a directory: {}", targetDirectory.getAbsolutePath());
            return false;
        }

        boolean retVal = false;
        int tryTimes = 3;

        while (!retVal && tryTimes > 0) {
            tryTimes--;
            try {
                if (targetDirectory.exists()) {
                    FileUtils.deleteDirectory(targetDirectory);
                }
                FileUtils.copyDirectory(sourceDirectory, targetDirectory);
                retVal = true;
            } catch (IOException e) {
                log.error(ExceptionUtils.getStackTrace(e));
            }
        }
        return retVal;
    }

    public String digest(File f) {
        boolean retVal = false;
        int tryTimes = 3;
        String md5Hex = null;
        while (!retVal && tryTimes > 0) {
            tryTimes--;
            try {
                md5Hex = _digester.digestAsHex(f);
                PrettyPrinter.info(f.getAbsolutePath() + "\t" + md5Hex);
                log.debug("Digest Succeed: {}, digest: {}", f.getAbsolutePath(), md5Hex);
                retVal = true;
            } catch (IOException e) {
                retVal = false;
            }
        }
        return md5Hex;
    }

    public void setMetTemplate(Template metTemplate) {
        this.metTemplate = metTemplate;
    }

    public File getSrcFolder() {
        return srcFolder;
    }

    public void setSrcFolder(File srcFolder) {
        this.srcFolder = srcFolder;
    }

    public File getDestFolder() {
        return destFolder;
    }

    public void setDestFolder(File destFolder) {
        this.destFolder = destFolder;
    }

    public boolean isForcedReplaced() {
        return isForcedReplaced;
    }

    public void setForcedReplaced(boolean forcedReplaced) {
        isForcedReplaced = forcedReplaced;
    }

    public Template getMetTemplate() {
        return metTemplate;
    }
}
