package nz.govt.natlib.ajhr.proc;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import nz.govt.natlib.ajhr.metadata.MetadataMetProp;
import nz.govt.natlib.ajhr.metadata.MetadataRetVal;
import nz.govt.natlib.ajhr.metadata.MetadataSipItem;
import nz.govt.natlib.ajhr.metadata.PapersPastTitle;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MetsGenerationHandler {
    private static final Logger log = LoggerFactory.getLogger(MetsGenerationHandler.class);

    public static final String PRESERVATION_MASTER_FOLDER = "PM_01";
    public static final String MODIFIED_MASTER_FOLDER = "MM_01";
    public static final String READY_FOR_INGESTION_MARK = "ready-for-permissions";
    private static final String STREAM_FOLDER = "content" + File.separator + "streams";
    private static final String PRESERVATION_MASTER_STREAM_FOLDER = STREAM_FOLDER + File.separator + PRESERVATION_MASTER_FOLDER;
//    private static final String MODIFIED_MASTER_STREAM_FOLDER = STREAM_FOLDER + File.separator + MODIFIED_MASTER_FOLDER;

    private static DigestUtils _digester = null;

    static {
        try {
            _digester = new DigestUtils(MessageDigest.getInstance("MD5"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    private Template metTemplate;
    private File subFolder;
    private File targetRootLocation;
    private boolean isForced = false;
//    private int startDate;
//    private int endDate;
    private PapersPastTitle papersPastTitle;
    private boolean reprocess;

    public MetsGenerationHandler(Template metTemplate, File subFolder, File targetRootLocation, boolean isForced,
                                 PapersPastTitle papersPastTitle, boolean reprocess) {
        this.metTemplate = metTemplate;
        this.subFolder = subFolder;
        this.targetRootLocation = targetRootLocation;
        this.isForced = isForced;
//        this.startDate = startDate;
//        this.endDate = endDate;
        this.papersPastTitle = papersPastTitle;
        this.reprocess = reprocess;
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

//        boolean retVal;
//        File pmSourceFolder = AJHRUtils.combinePath(this.subFolder, PRESERVATION_MASTER_FOLDER);
//        File pmTargetContentStreamFolder = AJHRUtils.combinePath(targetRootLocation, PRESERVATION_MASTER_STREAM_FOLDER);
//        retVal = copyDirectory(pmSourceFolder, pmTargetContentStreamFolder);
//        if (!retVal) {
//            log.error("Failed to copy folder: {} -> {}", pmSourceFolder.getAbsolutePath(), pmTargetContentStreamFolder.getAbsolutePath());
//            return MetadataRetVal.FAIL;
//        } else {
//            log.debug("Copy folder: {} -> {}", pmSourceFolder.getAbsolutePath(), pmTargetContentStreamFolder.getAbsolutePath());
//        }
//
//        File mmSourceFolder = AJHRUtils.combinePath(this.subFolder, MODIFIED_MASTER_FOLDER);
//        File mmTargetContentStreamFolder = AJHRUtils.combinePath(targetRootLocation, MODIFIED_MASTER_STREAM_FOLDER);
//        retVal = copyDirectory(mmSourceFolder, mmTargetContentStreamFolder);
//        if (!retVal) {
//            log.error("Failed to copy folder: {} -> {}", mmSourceFolder.getAbsolutePath(), mmTargetContentStreamFolder.getAbsolutePath());
//            return MetadataRetVal.FAIL;
//        } else {
//            log.debug("Copy folder: {} -> {}", mmSourceFolder.getAbsolutePath(), mmTargetContentStreamFolder.getAbsolutePath());
//        }

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
        String issue = this.targetRootLocation.getName();
        if (reprocess && issue.endsWith("_reprocess")) {
            issue = issue.replaceAll("_reprocess$", "");
        }

        MetadataMetProp metProp = MetadataMetProp.getInstance(issue, this.papersPastTitle);

        File[] children = this.subFolder.listFiles();
        File PMFolder;

        if (children == null || children.length == 0) {
            System.out.println("Folder is empty.");
            throw new IOException("The directory is empty: " + this.subFolder.getAbsolutePath());
        }

        if (children[0].isDirectory()) {
            PMFolder =  AJHRUtils.combinePath(this.subFolder, "PM_01");
        } else PMFolder = this.subFolder;

        List<MetadataSipItem> pmList = handleFiles(metProp, PMFolder, AJHRUtils.combinePath(this.targetRootLocation, PRESERVATION_MASTER_STREAM_FOLDER));
//        List<MetadataSipItem> mmList = handleFiles(metProp, AJHRUtils.combinePath(this.subFolder, MODIFIED_MASTER_FOLDER), AJHRUtils.combinePath(this.targetRootLocation, MODIFIED_MASTER_STREAM_FOLDER));

        ModelMap model = new ModelMap();
        model.addAttribute("metProp", metProp);
        model.addAttribute("pmList", pmList);
//        model.addAttribute("mmList", mmList);

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

        // Sort numerically by filename
        Arrays.sort(files, Comparator.comparingInt(f -> {
            String name = f.getName();

            // Remove extension if any
            int dotIndex = name.lastIndexOf('.');
            if (dotIndex != -1) {
                name = name.substring(0, dotIndex);
            }

            // If letters may exist after the number (e.g., 0004a), strip them
            String numericPart = name.replaceAll("(\\d+).*", "$1");

            // If no digits, push to end
            if (numericPart.isEmpty() || !numericPart.matches("\\d+")) {
                return Integer.MAX_VALUE;
            }

            return Integer.parseInt(numericPart);
        }));

        List<File> pageFiles = new ArrayList<>(files.length);
        boolean atStart = true;


        for (File f : files) {
            if (atStart) {
                if (f.length() < 15 * 1024) {
                    log.debug("Skipping initial small cover: {} ({} bytes)", f.getName(), f.length());
                    // Don't add; continue trying to find the first real file
                    continue;
                }
            }

            atStart = false;
            pageFiles.add(f);
        }

        if (pageFiles.isEmpty()) {
            throw new IOException("No valid page files to process in: " + srcDirectory.getAbsolutePath());
        }

//        String firstFileName = files[1].getName();
        String firstFileName = pageFiles.get(0).getName();
        String base = AJHRUtils.removeExtension(firstFileName);
        String digits = base.replaceAll("(\\d+).*", "$1"); // take only leading digits
        String trimmed = digits.replaceFirst("0+$", "");

        boolean needsNormalization = digits.length() > 4 && trimmed.length() <= 4;

        int fileId = 1;
        for (File f : pageFiles) {
            if (needsNormalization && f.length() < 15 * 1024) {
                continue; // skip this file
            }
            if (f.getName().toLowerCase().endsWith(".tif")) {
                String fileName = AJHRUtils.correctFilename(f, fileId, needsNormalization);
                String label = fileName.replaceAll("(?<!^)[.].*", "");
                MetsAtomicFileHandler fileAtomicHandler = new MetsAtomicFileHandler(f, new File(destDirectory, fileName));
                boolean retVal = fileAtomicHandler.md5DigestAndCopy();

                MetadataSipItem item = new MetadataSipItem();
                item.setFile(f);
                item.setFileId(fileId++);
                item.setFileOriginalName(fileName);
                item.setFileEntityType(getFileEntityTypeFromExt(f.getName()));
                item.setFileSize(Long.toString(f.length()));
                item.setFixityValue(fileAtomicHandler.getDigestString());
                item.setLabel(label);


//            if (fileName.toLowerCase().startsWith(metProp.getAccrualPeriodicity().toLowerCase())) {
//                item.setLabel(fileName.substring(metProp.getAccrualPeriodicity().length() + 1));
//            } else {
//                item.setLabel(fileName);
//            }
                list.add(item);
            }
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

    private static int extractNumeric(String name) {
        String numericPart = name.replaceAll("^(\\d+).*", "$1");
        return Integer.parseInt(numericPart);
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

//    public File getRootDirectory() {
//        return rootDirectory;
//    }
//
//    public void setRootDirectory(File rootDirectory) {
//        this.rootDirectory = rootDirectory;
//    }

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
