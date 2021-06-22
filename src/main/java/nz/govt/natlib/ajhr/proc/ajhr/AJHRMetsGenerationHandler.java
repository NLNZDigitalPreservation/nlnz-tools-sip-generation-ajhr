package nz.govt.natlib.ajhr.proc.ajhr;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import nz.govt.natlib.ajhr.metadata.MetadataMetProp;
import nz.govt.natlib.ajhr.metadata.MetadataSipItem;
import nz.govt.natlib.ajhr.proc.AbstractMetsGenerationHandler;
import nz.govt.natlib.ajhr.util.MetsUtils;
import org.springframework.ui.ModelMap;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class AJHRMetsGenerationHandler extends AbstractMetsGenerationHandler {
    protected File rootDirectory;

    public AJHRMetsGenerationHandler(Template metTemplate, File rootDirectory, File srcFolder, String targetRootLocation, boolean isForcedReplaced) {
        String sipFolder = String.format("%s-%s", rootDirectory.getName(), srcFolder.getName());
        this.destFolder = MetsUtils.combinePath(targetRootLocation, sipFolder);
        this.metTemplate = metTemplate;
        this.rootDirectory = rootDirectory;
        this.srcFolder = srcFolder;
        this.isForcedReplaced = isForcedReplaced;
    }

    public String createMetsXmlAndCopyStreams() throws IOException, TemplateException, NoSuchAlgorithmException {
        MetadataMetProp metProp = MetadataMetProp.getInstance(this.rootDirectory.getName(), this.srcFolder.getName());
        List<MetadataSipItem> pmList = handleFiles(metProp, MetsUtils.combinePath(this.srcFolder, PRESERVATION_MASTER_FOLDER), MetsUtils.combinePath(this.destFolder, PRESERVATION_MASTER_STREAM_FOLDER));
        List<MetadataSipItem> mmList = handleFiles(metProp, MetsUtils.combinePath(this.srcFolder, MODIFIED_MASTER_FOLDER), MetsUtils.combinePath(this.destFolder, MODIFIED_MASTER_STREAM_FOLDER));

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
            AJHRMetsAtomicFileHandler fileAtomicHandler = new AJHRMetsAtomicFileHandler(f, new File(destDirectory, f.getName()));
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

    public File getRootDirectory() {
        return rootDirectory;
    }

    public void setRootDirectory(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }
}
