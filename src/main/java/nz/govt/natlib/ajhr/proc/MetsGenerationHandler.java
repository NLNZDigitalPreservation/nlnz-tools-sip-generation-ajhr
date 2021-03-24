package nz.govt.natlib.ajhr.proc;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import nz.govt.natlib.ajhr.metadata.MetadataMetProp;
import nz.govt.natlib.ajhr.metadata.MetadataRetVal;
import nz.govt.natlib.ajhr.metadata.MetadataSipItem;
import nz.govt.natlib.ajhr.util.AJHRUtils;
import nz.govt.natlib.ajhr.util.LogPrinter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.ui.ModelMap;
import org.springframework.util.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MetsGenerationHandler {
    public static final String PRESERVATION_MASTER_FOLDER = "PM_01";
    public static final String MODIFIED_MASTER_FOLDER = "MM_01";
    public static final String READY_FOR_INGESTION_MARK = "ready-for-ingestion-FOLDER-COMPLETED";
    private static final String STREAM_FOLDER = "content" + File.separator + "streams";
    private static final String PRESERVATION_MASTER_STREAM_FOLDER = STREAM_FOLDER + File.separator + PRESERVATION_MASTER_FOLDER;
    private static final String MODIFIED_MASTER_STREAM_FOLDER = STREAM_FOLDER + File.separator + MODIFIED_MASTER_FOLDER;

    private Template metTemplate;
    private File rootDirectory;
    private File subFolder;
    private File targetRootLocation;
    private boolean isForced = false;

    public MetsGenerationHandler(Template metTemplate, File rootDirectory, File subFolder, String targetRootLocation, boolean isForced) {
        this.metTemplate = metTemplate;
        this.rootDirectory = rootDirectory;
        this.subFolder = subFolder;
        String sipFolder = String.format("%s-%s", this.rootDirectory.getName(), this.subFolder.getName());
        this.targetRootLocation = AJHRUtils.combinePath(targetRootLocation, sipFolder);
        this.isForced = isForced;
    }

    public MetadataRetVal process() throws IOException, TemplateException {
        File readForIngestionMarkFile = AJHRUtils.combinePath(subFolder, READY_FOR_INGESTION_MARK);
        if (readForIngestionMarkFile.exists() && !isForced) {
            LogPrinter.info("Skip " + subFolder.getAbsolutePath());
            return MetadataRetVal.SKIPPED;
        }

        //Clean the existing target location
        if (this.targetRootLocation.exists()) {
            FileUtils.deleteDirectory(this.targetRootLocation);
        }

        String metsXml = createMetsXml();

        boolean retVal;
        File pmSourceFolder = AJHRUtils.combinePath(this.subFolder, PRESERVATION_MASTER_FOLDER);
        File pmTargetContentStreamFolder = AJHRUtils.combinePath(targetRootLocation, PRESERVATION_MASTER_STREAM_FOLDER);
        retVal = copyDirectory(pmSourceFolder, pmTargetContentStreamFolder);
        if (!retVal) {
            return MetadataRetVal.FAILED;
        }

        File mmSourceFolder = AJHRUtils.combinePath(this.subFolder, MODIFIED_MASTER_FOLDER);
        File mmTargetContentStreamFolder = AJHRUtils.combinePath(targetRootLocation, MODIFIED_MASTER_STREAM_FOLDER);
        retVal = copyDirectory(mmSourceFolder, mmTargetContentStreamFolder);
        if (!retVal) {
            return MetadataRetVal.FAILED;
        }

        //Write mets xml
        File targetMetsXmlFile = AJHRUtils.combinePath(this.targetRootLocation, "content", "mets.xml");
        FileUtils.writeStringToFile(targetMetsXmlFile, metsXml, StandardCharsets.UTF_8);

        //Write ready file to sip folder
        File targetReadyFile = AJHRUtils.combinePath(this.targetRootLocation, READY_FOR_INGESTION_MARK);
        FileUtils.writeByteArrayToFile(targetReadyFile, new byte[0]);

        //Mark as finished
        File sourceReadyFile = AJHRUtils.combinePath(this.subFolder, READY_FOR_INGESTION_MARK);
        FileUtils.writeByteArrayToFile(sourceReadyFile, new byte[0]);

        return MetadataRetVal.SUCCEED;
    }

    public boolean copyDirectory(File sourceDirectory, File targetDirectory) {
        if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
            LogPrinter.error("The source directory does not exist: " + sourceDirectory.getAbsolutePath());
            return false;
        }

        if (targetDirectory.exists() && !sourceDirectory.isDirectory()) {
            LogPrinter.error("The target directory is not a directory: " + targetDirectory.getAbsolutePath());
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
                LogPrinter.error(ExceptionUtils.getStackTrace(e));
            }
        }
        return retVal;
    }

    public String createMetsXml() throws IOException, TemplateException {
        MetadataMetProp metProp = MetadataMetProp.getInstance(this.rootDirectory.getName(), this.subFolder.getName());
        List<MetadataSipItem> pmList = handleFiles(AJHRUtils.combinePath(this.subFolder, PRESERVATION_MASTER_FOLDER));
        List<MetadataSipItem> mmList = handleFiles(AJHRUtils.combinePath(this.subFolder, MODIFIED_MASTER_FOLDER));

        ModelMap model = new ModelMap();
        model.addAttribute("metProp", metProp);
        model.addAttribute("pmList", pmList);
        model.addAttribute("mmList", mmList);

        StringWriter writer = new StringWriter();

        this.metTemplate.process(model, writer);
        return writer.toString();
    }

    public List<MetadataSipItem> handleFiles(File directory) throws IOException {
        List<MetadataSipItem> list = new ArrayList<>();

        File[] files = directory.listFiles();
        if (files == null) {
            LogPrinter.error("The directory is empty: " + directory.getAbsolutePath());
            throw new IOException("The directory is empty: " + directory.getAbsolutePath());
        }

        int fileId = 1;
        for (File f : files) {
            MetadataSipItem item = new MetadataSipItem();
            item.setFile(f);
            item.setFileId(fileId++);
            item.setFileOriginalName(f.getName());
            item.setFileEntityType(getFileEntityTypeFromExt(f.getName()));
            item.setFileSize(Long.toString(f.length()));
            item.setFixityValue(digest(f));

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
                byte[] md5 = DigestUtils.md5Digest(new FileInputStream(f));
                md5Hex = DigestUtils.md5DigestAsHex(md5);
                LogPrinter.debug("Digest Succeed: " + f.getAbsolutePath());
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
