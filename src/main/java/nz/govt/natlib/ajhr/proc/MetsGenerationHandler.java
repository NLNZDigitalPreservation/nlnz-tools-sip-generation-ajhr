package nz.govt.natlib.ajhr.proc;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import nz.govt.natlib.ajhr.metadata.MetadataMetProp;
import nz.govt.natlib.ajhr.metadata.MetadataSipItem;
import nz.govt.natlib.ajhr.util.AJHRUtils;
import nz.govt.natlib.ajhr.util.LogPrinter;
import org.springframework.ui.ModelMap;
import org.springframework.util.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MetsGenerationHandler implements Runnable {
    private static final String READY_FOR_INGESTION_MARK = "ready-for-ingestion-FOLDER-COMPLETED";
    private static final String PRESERVATION_MASTER_FOLDER = "PM_01";
    private static final String MODIFIED_MASTER_FOLDER = "MM_01";
    private static final String STREAM_FOLDER = "content" + File.separator + "streams";
    private static final String PRESERVATION_MASTER_STREAM_FOLDER = STREAM_FOLDER + File.separator + PRESERVATION_MASTER_FOLDER;
    private static final String MODIFIED_MASTER_STREAM_FOLDER = STREAM_FOLDER + File.separator + MODIFIED_MASTER_FOLDER;

    private Template metTemplate;
    private File rootDirectory;
    private File subFolder;
    private File targetRootLocation;
    private boolean isForced = false;

    public MetsGenerationHandler(Template metTemplate, File rootDirectory, File subFolder, String targetRootLocation) {
        this.metTemplate = metTemplate;
        this.rootDirectory = rootDirectory;
        this.subFolder = subFolder;
        String sipFolder = String.format("%s-%s", this.rootDirectory.getName(), this.subFolder.getName());
        this.targetRootLocation = AJHRUtils.combinePath(targetRootLocation, sipFolder);
    }

    @Override
    public void run() {
        try {
            process();
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }
    }

    public void process() throws IOException, TemplateException {
        File readForIngestionMarkFile = AJHRUtils.combinePath(subFolder, READY_FOR_INGESTION_MARK);
        if (readForIngestionMarkFile.exists() && !isForced) {
            LogPrinter.info("Skip " + subFolder.getAbsolutePath());
            return;
        }

        String metsXml = createMetsXml();

        File pmSourceFolder = AJHRUtils.combinePath(this.subFolder, PRESERVATION_MASTER_FOLDER);
        File pmTargetContentStreamFolder = AJHRUtils.combinePath(targetRootLocation, PRESERVATION_MASTER_STREAM_FOLDER);
        boolean retVal = pmTargetContentStreamFolder.mkdirs();
        if (!retVal) {
            LogPrinter.error("Failed to create directory: " + pmTargetContentStreamFolder.getAbsolutePath());
            return;
        }
        FileUtils.copyDirectory(pmSourceFolder, pmTargetContentStreamFolder);

        File mmSourceFolder = AJHRUtils.combinePath(this.subFolder, MODIFIED_MASTER_FOLDER);
        File mmTargetContentStreamFolder = AJHRUtils.combinePath(targetRootLocation, MODIFIED_MASTER_STREAM_FOLDER);
        retVal = mmTargetContentStreamFolder.mkdirs();
        if (!retVal) {
            LogPrinter.error("Failed to create directory: " + mmTargetContentStreamFolder.getAbsolutePath());
            return;
        }
        FileUtils.copyDirectory(mmSourceFolder, mmTargetContentStreamFolder);

        //Write mets xml
        File targetMetsXmlFile = AJHRUtils.combinePath(this.targetRootLocation, "content", "mets.xml");
        FileUtils.writeStringToFile(targetMetsXmlFile, metsXml, StandardCharsets.UTF_8);

        //Write ready file to sip folder
        File targetReadyFile = AJHRUtils.combinePath(this.targetRootLocation, READY_FOR_INGESTION_MARK);
        FileUtils.writeByteArrayToFile(targetReadyFile, new byte[0]);

        //Mark as finished
        File sourceReadyFile = AJHRUtils.combinePath(this.subFolder, READY_FOR_INGESTION_MARK);
        FileUtils.writeByteArrayToFile(sourceReadyFile, new byte[0]);
    }

    public String createMetsXml() throws IOException, TemplateException {
        MetadataMetProp metProp = parseMetProp();
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

    public MetadataMetProp parseMetProp() {
        String rootFolderName = this.rootDirectory.getName();

        int idxStart = 0, idxEnd = rootFolderName.indexOf('_', idxStart);
        String title = rootFolderName.substring(idxStart, idxEnd);

        idxStart = idxEnd + 1;
        idxEnd = rootFolderName.indexOf('_', idxStart);
        String year = rootFolderName.substring(idxStart, idxEnd);

        idxStart = idxEnd + 1;
        String volume = rootFolderName.substring(idxStart);

        String accrualPeriodicity = this.subFolder.getName();

        MetadataMetProp metProp = new MetadataMetProp();
        metProp.setTitle(title);
        metProp.setYear(year);
        metProp.setVolume(volume);
        metProp.setAccrualPeriodicity(accrualPeriodicity);

        return metProp;
    }

    public List<MetadataSipItem> handleFiles(File directory) throws IOException {
        List<MetadataSipItem> list = new ArrayList<>();

        File[] files = directory.listFiles();
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

    public String digest(File f) throws IOException {
        byte[] md5 = DigestUtils.md5Digest(new FileInputStream(f));
        String md5Hex = DigestUtils.md5DigestAsHex(md5);
        LogPrinter.info("Digest Succeed: " + f.getAbsolutePath());
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
