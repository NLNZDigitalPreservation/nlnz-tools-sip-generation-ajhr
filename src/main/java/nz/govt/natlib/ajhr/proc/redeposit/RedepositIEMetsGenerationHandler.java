package nz.govt.natlib.ajhr.proc.redeposit;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import nz.govt.natlib.ajhr.metadata.MetadataSipItem;
import nz.govt.natlib.ajhr.metadata.RedepositIeDTO;
import nz.govt.natlib.ajhr.proc.AbstractMetsGenerationHandler;
import nz.govt.natlib.ajhr.util.MetsUtils;
import nz.govt.natlib.ajhr.util.MultiThreadsPrint;
import nz.govt.natlib.ajhr.util.PrettyPrinter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.ui.ModelMap;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class RedepositIEMetsGenerationHandler extends AbstractMetsGenerationHandler {
    private static final int STREAM_BUFFER_LENGTH = 1024 * 16;
    private final RedepositIeDTO redepositIeDTO;

    public RedepositIEMetsGenerationHandler(Template metTemplate, String srcFolder, String destFolder, RedepositIeDTO redepositIeDTO, boolean replaceFlag) {
        this.metTemplate = metTemplate;
        this.srcFolder = new File(srcFolder);
        this.destFolder = new File(destFolder);
        this.redepositIeDTO = redepositIeDTO;
        this.isForcedReplaced = replaceFlag;
    }

    @Override
    protected String createMetsXmlAndCopyStreams() throws IOException, TemplateException, NoSuchAlgorithmException {
        ModelMap model = new ModelMap();
        model.addAttribute("metProp", redepositIeDTO);

        File pmFolder = MetsUtils.combinePath(this.srcFolder, PRESERVATION_MASTER_FOLDER);
        if (pmFolder.exists()) {
            this.copyDirectory(pmFolder, MetsUtils.combinePath(this.destFolder, PRESERVATION_MASTER_STREAM_FOLDER));
            List<MetadataSipItem> pmList = handleFiles(MetsUtils.combinePath(this.srcFolder, PRESERVATION_MASTER_FOLDER));
            if (pmList.size() != redepositIeDTO.getNumFiles()) {
                throw new IOException("The number of files in the folder does not match the number in the sheet: " + pmFolder.getAbsolutePath());
            }
            model.addAttribute("pmList", pmList);
        } else {
            throw new IOException("The preservation master folder does not exist: " + pmFolder.getAbsolutePath());
        }

        File mmFolder = MetsUtils.combinePath(this.srcFolder, MODIFIED_MASTER_FOLDER);
        if (mmFolder.exists()) {
            this.copyDirectory(mmFolder, MetsUtils.combinePath(this.destFolder, MODIFIED_MASTER_FOLDER));
            List<MetadataSipItem> mmList = handleFiles(MetsUtils.combinePath(this.srcFolder, MODIFIED_MASTER_FOLDER));
            model.addAttribute("mmList", mmList);
        } else {
            model.addAttribute("mmList", new ArrayList<MetadataSipItem>());
        }

        StringWriter writer = new StringWriter();
        this.metTemplate.process(model, writer);
        return writer.toString();
    }

    public List<MetadataSipItem> handleFiles(File srcDirectory) throws IOException, NoSuchAlgorithmException {
        List<MetadataSipItem> list = new ArrayList<>();

        File[] files = srcDirectory.listFiles();
        if (files == null) {
            log.error("The directory is empty: {}", srcDirectory.getAbsolutePath());
            throw new IOException("The directory is empty: " + srcDirectory.getAbsolutePath());
        }

        int fileId = 1;
        for (File f : files) {
            String fileName = f.getName();

            MetadataSipItem item = new MetadataSipItem();
            item.setFile(f);
            item.setFileId(fileId++);
            item.setFileOriginalName(fileName);
            item.setFileSize(Long.toString(f.length()));

            int idx = fileName.indexOf('.');
            if (idx > 0) {
                item.setLabel(fileName.substring(0, idx));
            } else {
                item.setLabel(fileName);
            }
            list.add(item);

            //TODO: Append the properties inside the sheet
        }
        return list;
    }

    public boolean copyDirectory(File srcSubFolder, File destSubFolder) {
        File[] files = srcSubFolder.listFiles();
        if (files == null) {
            return false;
        }

        if (!destSubFolder.exists() && !destSubFolder.mkdirs()) {
            return false;
        }

        for (File f : files) {
            boolean copyRstVal = false;
            try {
                copyRstVal = copyFile(f, new File(destSubFolder, f.getName()));
            } catch (IOException e) {
                PrettyPrinter.error(log, "Failed to copy file: {} -> {} ", ExceptionUtils.getStackTrace(e));
                return false;
            }
            if (!copyRstVal) {
                return false;
            }
        }
        return true;
    }

    private boolean copyFile(File srcFile, File destFile) throws IOException {
        InputStream inputStream = new BufferedInputStream(new FileInputStream(srcFile));
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(destFile));

        final byte[] buffer = new byte[STREAM_BUFFER_LENGTH];
        int read = inputStream.read(buffer, 0, STREAM_BUFFER_LENGTH);
        double lenTotal = srcFile.length(), lenCurrentRead = 0;

        String msg = String.format("%s [%.2f%s]", srcFile.getAbsolutePath(), lenCurrentRead * 100 / lenTotal, "%");
        String key = MultiThreadsPrint.putUnFinished(msg);
        while (read > -1) {
            outputStream.write(buffer, 0, read);

            lenCurrentRead += read;

            msg = String.format("%s [%.2f%s]", srcFile.getAbsolutePath(), lenCurrentRead * 100 / lenTotal, "%");
            MultiThreadsPrint.putUnFinished(key, msg);

            read = inputStream.read(buffer, 0, STREAM_BUFFER_LENGTH);
        }

        outputStream.close();
        inputStream.close();

        msg = String.format("%s 100%s size=%d", srcFile.getAbsolutePath(), "%", (long) lenTotal);
        MultiThreadsPrint.putFinished(key, msg);

        return true;
    }
}
