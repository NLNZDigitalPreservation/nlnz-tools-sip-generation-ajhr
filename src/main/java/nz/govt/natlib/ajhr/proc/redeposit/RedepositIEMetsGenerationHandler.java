package nz.govt.natlib.ajhr.proc.redeposit;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import nz.govt.natlib.ajhr.metadata.MetadataSipItem;
import nz.govt.natlib.ajhr.metadata.RedepositIeDTO;
import nz.govt.natlib.ajhr.metadata.RedepositIeFileDTO;
import nz.govt.natlib.ajhr.proc.AbstractMetsGenerationHandler;
import nz.govt.natlib.ajhr.util.MetsUtils;
import nz.govt.natlib.ajhr.util.MultiThreadsPrint;
import nz.govt.natlib.ajhr.util.PrettyPrinter;
import org.springframework.ui.ModelMap;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedepositIEMetsGenerationHandler extends AbstractMetsGenerationHandler {
    private static final int STREAM_BUFFER_LENGTH = 1024 * 16;
    private final RedepositIeDTO redepositIeDTO;
    private final boolean isMultiple;

    public RedepositIEMetsGenerationHandler(Template metTemplate, String srcFolder, String destFolder, RedepositIeDTO redepositIeDTO, boolean replaceFlag, boolean isMultiple) {
        this.metTemplate = metTemplate;
        this.srcFolder = new File(srcFolder);
        this.destFolder = new File(destFolder);
        this.redepositIeDTO = redepositIeDTO;
        this.isForcedReplaced = replaceFlag;
        this.isMultiple = isMultiple;
    }

    @Override
    protected String createMetsXmlAndCopyStreams() throws IOException, TemplateException {
        ModelMap model = new ModelMap();
        model.addAttribute("metProp", redepositIeDTO);

        File pmFolder = MetsUtils.combinePath(this.srcFolder, PRESERVATION_MASTER_FOLDER);
        if (pmFolder.exists()) {
            List<RedepositIeFileDTO> pmList = new ArrayList<>();
            this.walkFiles(pmList, MetsUtils.combinePath(this.srcFolder, PRESERVATION_MASTER_FOLDER));

            if (!isMultiple) {
                pmList.forEach(e -> {
                    e.setFileCreationDate(redepositIeDTO.getFileCreationDate());
                    e.setFileModificationDate(redepositIeDTO.getFileModificationDate());
                });
            } else {
                Map<String, RedepositIeFileDTO> mapActual = new HashMap<>();
                pmList.forEach(item -> mapActual.put(item.getFileOriginalName(), item));
                pmList.clear();

                pmList = redepositIeDTO.getFiles();

                //Validate that the number of files in excel and the number of actual files are consistent
                if (pmList.size() != redepositIeDTO.getNumFiles() || pmList.size() != mapActual.size()) {
                    throw new IOException(String.format("The number of files in excel is %d, the number of files in the folder %s is %d, and the number of files in excel is %d", redepositIeDTO.getNumFiles(), pmFolder.getAbsolutePath(), mapActual.size(), pmList.size()));
                }

                //Validate that all files exist in the actual folder
                for (RedepositIeFileDTO item : pmList) {
                    if (!mapActual.containsKey(item.getFileOriginalName())) {
                        throw new IOException(String.format("The file %s does not exist", item.getFileOriginalName()));
                    }
                }
            }

            model.addAttribute("pmList", pmList);
        } else {
            throw new IOException("The preservation master folder does not exist: " + pmFolder.getAbsolutePath());
        }

        File mmFolder = MetsUtils.combinePath(this.srcFolder, MODIFIED_MASTER_FOLDER);
        if (mmFolder.exists()) {
            List<RedepositIeFileDTO> mmList = new ArrayList<>();
            this.walkFiles(mmList, MetsUtils.combinePath(this.srcFolder, MODIFIED_MASTER_FOLDER));
            model.addAttribute("mmList", mmList);
        } else {
            model.addAttribute("mmList", new ArrayList<MetadataSipItem>());
        }

        StringWriter writer = new StringWriter();
        this.metTemplate.process(model, writer);

        //=================================If the mets.xml can be generated, then copy files=================================
        if (pmFolder.exists()) {
            this.copyDirectory(pmFolder, MetsUtils.combinePath(this.destFolder, PRESERVATION_MASTER_STREAM_FOLDER));
        }

        if (mmFolder.exists()) {
            this.copyDirectory(mmFolder, MetsUtils.combinePath(this.destFolder, MODIFIED_MASTER_STREAM_FOLDER));
        }
        //================================End of copying files===============================================================

        return writer.toString();
    }

    public void walkFiles(List<RedepositIeFileDTO> retVal, File srcPath) throws IOException {
        if (srcPath.isFile()) {
            RedepositIeFileDTO dto = getFileProperties(srcPath, retVal.size() + 1);
            retVal.add(dto);
            return;
        }

        File[] files = srcPath.listFiles();
        if (files == null) {
            log.warn("The directory is empty: {}", srcPath.getAbsolutePath());
            return;
        }
        for (File f : files) {
            walkFiles(retVal, f);
        }
    }

    public RedepositIeFileDTO getFileProperties(File f, int fileId) {
        String fileName = f.getName();

        RedepositIeFileDTO item = new RedepositIeFileDTO();
        item.setFile(f);
        item.setFileId(fileId);
        item.setFileOriginalName(fileName);
        item.setFileSize(Long.toString(f.length()));

        int idx = fileName.indexOf('.');
        if (idx > 0) {
            item.setLabel(fileName.substring(0, idx));
        } else {
            item.setLabel(fileName);
        }

        return item;
    }

    public boolean copyDirectory(File srcSubFolder, File destSubFolder) {
        if (srcSubFolder.isDirectory()) {
            if (!destSubFolder.mkdirs()) {
                PrettyPrinter.error(log, "Failed to make directory: {}", destSubFolder.getAbsolutePath());
                return false;
            }

            File[] files = srcSubFolder.listFiles();
            if (files != null) {
                for (File f : files) {
                    copyDirectory(f, new File(destSubFolder, f.getName()));
                }
            }
        } else {
            try {
                copyFile(srcSubFolder, destSubFolder);
            } catch (IOException e) {
                PrettyPrinter.error(log, e, "Failed to copy file: {} -> {} ", srcSubFolder.getAbsolutePath(), destSubFolder.getAbsolutePath());
                return false;
            }
        }

        return true;
    }

    private void copyFile(File srcFile, File destFile) throws IOException {
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
    }
}
