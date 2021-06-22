package nz.govt.natlib.ajhr.proc.redeposit;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import nz.govt.natlib.ajhr.metadata.MetadataSipItem;
import nz.govt.natlib.ajhr.metadata.RedepositIeDTO;
import nz.govt.natlib.ajhr.proc.AbstractMetsGenerationHandler;
import nz.govt.natlib.ajhr.util.MetsUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class RedepositIEMetsGenerationHandler extends AbstractMetsGenerationHandler {
    private static final Logger log = LoggerFactory.getLogger(RedepositIEMetsGenerationHandler.class);
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
            FileUtils.copyDirectory(pmFolder, MetsUtils.combinePath(this.destFolder, PRESERVATION_MASTER_STREAM_FOLDER));
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
            FileUtils.copyDirectory(mmFolder, MetsUtils.combinePath(this.destFolder, MODIFIED_MASTER_FOLDER));
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

}
