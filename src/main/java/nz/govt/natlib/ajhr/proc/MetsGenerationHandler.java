package nz.govt.natlib.ajhr.proc;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import nz.govt.natlib.ajhr.metadata.MetadataMetProp;
import nz.govt.natlib.ajhr.metadata.MetadataRetVal;
import nz.govt.natlib.ajhr.metadata.MetadataSipItem;
import nz.govt.natlib.ajhr.util.AJHRUtils;
import nz.govt.natlib.ajhr.util.PrettyPrinter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MetsGenerationHandler {
    private static final Logger log = LoggerFactory.getLogger(MetsGenerationHandler.class);

    public static final String PRESERVATION_MASTER_FOLDER = "PM_01";
    public static final String MODIFIED_MASTER_FOLDER = "MM_01";
    public static final String READY_FOR_INGESTION_MARK = "ready-for-ingestion-FOLDER-COMPLETED";
    private static final String STREAM_FOLDER = "content" + File.separator + "streams";
    private static final String PRESERVATION_MASTER_STREAM_FOLDER = STREAM_FOLDER + File.separator + PRESERVATION_MASTER_FOLDER;
    private static final String MODIFIED_MASTER_STREAM_FOLDER = STREAM_FOLDER + File.separator + MODIFIED_MASTER_FOLDER;

    private static DigestUtils _digester = null;

    static {
        try {
            _digester = new DigestUtils(MessageDigest.getInstance("MD5"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    private Template metTemplate;
    private File rootDirectory;
    private File subFolder;
    private File targetRootLocation;
    private boolean isForced = false;

    public MetsGenerationHandler(Template metTemplate, File rootDirectory, File subFolder, String targetRootLocation, boolean isForced) {
        String sipFolder = String.format("%s-%s", rootDirectory.getName(), subFolder.getName());
        this.targetRootLocation = AJHRUtils.combinePath(targetRootLocation, sipFolder);
        this.metTemplate = metTemplate;
        this.rootDirectory = rootDirectory;
        this.subFolder = subFolder;
        this.isForced = isForced;
    }

    public MetadataRetVal process() throws IOException, TemplateException, NoSuchAlgorithmException {
        File readyForIngestionMarkFile = AJHRUtils.combinePath(targetRootLocation, READY_FOR_INGESTION_MARK);
        if (readyForIngestionMarkFile.exists() && !isForced) {
            log.info("Skip {}", subFolder.getAbsolutePath());
            return MetadataRetVal.SKIP;
        }

        //Clean the existing target location
        if (this.targetRootLocation.exists()) {
            log.debug("Clear the existing folder: {}", this.targetRootLocation.getAbsolutePath());
            FileUtils.deleteDirectory(this.targetRootLocation);
        }

        String metsXml = createMetsXmlAndCopyStreams();

        //Write mets xml
        File targetMetsXmlFile = AJHRUtils.combinePath(this.targetRootLocation, "content", "mets.xml");
        FileUtils.writeStringToFile(targetMetsXmlFile, metsXml, StandardCharsets.UTF_8);

        //Write ready file to sip folder
        File targetReadyFile = AJHRUtils.combinePath(this.targetRootLocation, READY_FOR_INGESTION_MARK);
        FileUtils.writeByteArrayToFile(targetReadyFile, new byte[0]);

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

    public String createMetsXmlAndCopyStreams() throws IOException, TemplateException, NoSuchAlgorithmException {
        MetadataMetProp metProp = MetadataMetProp.getInstance(this.rootDirectory.getName(), this.subFolder.getName());
        List<MetadataSipItem> pmList = handleFiles(metProp, AJHRUtils.combinePath(this.subFolder, PRESERVATION_MASTER_FOLDER), AJHRUtils.combinePath(this.targetRootLocation, PRESERVATION_MASTER_STREAM_FOLDER));
        List<MetadataSipItem> mmList = handleFiles(metProp, AJHRUtils.combinePath(this.subFolder, MODIFIED_MASTER_FOLDER), AJHRUtils.combinePath(this.targetRootLocation, MODIFIED_MASTER_STREAM_FOLDER));

        ModelMap model = new ModelMap();
        model.addAttribute("metProp", metProp);
        model.addAttribute("pmList", pmList);
        model.addAttribute("mmList", mmList);

        StringWriter writer = new StringWriter();

        this.metTemplate.process(model, writer);
        return writer.toString();
    }

    public List<MetadataSipItem> handleFiles(MetadataMetProp metProp, File srcDirectory, File destDirectory) throws IOException, NoSuchAlgorithmException {
        List<MetadataSipItem> list = new ArrayList<>();

        File[] files = srcDirectory.listFiles();
        if (files == null) {
            log.error("The directory is empty: {}", srcDirectory.getAbsolutePath());
            throw new IOException("The directory is empty: " + srcDirectory.getAbsolutePath());
        }

        int fileId = 1;
        for (File f : files) {
            String fileName = f.getName();
            MetsAtomicFileHandler fileAtomicHandler = new MetsAtomicFileHandler(f, new File(destDirectory, f.getName()));
            boolean retVal = fileAtomicHandler.md5DigestAndCopy();

            MetadataSipItem item = new MetadataSipItem();
            item.setFile(f);
            item.setFileId(fileId++);
            item.setFileOriginalName(fileName);
            item.setFileEntityType(getFileEntityTypeFromExt(f.getName()));
            item.setFileSize(Long.toString(f.length()));
            item.setFixityValue(fileAtomicHandler.getDigestString());

            if (fileName.toLowerCase().startsWith(metProp.getAccrualPeriodicity().toLowerCase())) {
                item.setLabel(fileName.substring(metProp.getAccrualPeriodicity().length() + 1));
            } else {
                item.setLabel(fileName);
            }
            list.add(item);
        }
        return list;
    }

    public String getFileEntityTypeFromExt(String fileName) {
        String lowerCaseFileName = fileName.toLowerCase();
        if (lowerCaseFileName.contains("mets.xml")) {
            return "METS";
        }
        if (lowerCaseFileName.endsWith(".tif")) {
            return "TIFF";
        }
        if (lowerCaseFileName.endsWith(".xml")) {
            return "ALTO";
        }
        return "Unknown";
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


    public Template getMetTemplate() {
        return metTemplate;
    }

    public void setMetTemplate(Template metTemplate) {
        this.metTemplate = metTemplate;
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    public void setRootDirectory(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public File getSubFolder() {
        return subFolder;
    }

    public void setSubFolder(File subFolder) {
        this.subFolder = subFolder;
    }

    public File getTargetRootLocation() {
        return targetRootLocation;
    }

    public void setTargetRootLocation(File targetRootLocation) {
        this.targetRootLocation = targetRootLocation;
    }

    public boolean isForced() {
        return isForced;
    }

    public void setForced(boolean forced) {
        isForced = forced;
    }
}
