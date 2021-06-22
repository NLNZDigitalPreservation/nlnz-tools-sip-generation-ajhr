package nz.govt.natlib.ajhr.proc.redeposit;

import freemarker.template.Template;
import nz.govt.natlib.ajhr.metadata.MetadataRetVal;
import nz.govt.natlib.ajhr.metadata.RedepositIeDTO;
import nz.govt.natlib.ajhr.metadata.ResultOverview;
import nz.govt.natlib.ajhr.proc.MetsTemplateService;
import nz.govt.natlib.ajhr.util.MetsUtils;
import nz.govt.natlib.ajhr.util.PrettyPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedepositIEFolderScanProcessor {
    private static final Logger log = LoggerFactory.getLogger(RedepositIEFolderScanProcessor.class);

    private MetsTemplateService MetsTemplateService;

    private List<RedepositIEEndPoint> endPoints = new ArrayList<>();

    public void process() {
        for (RedepositIEEndPoint endPoint : endPoints) {
            try {
                process(endPoint);
            } catch (IOException e) {
                log.error("Failed to process [{}]", endPoint.getSheetName(), e);
            }
        }
    }

    public void process(RedepositIEEndPoint endPoint) throws IOException {
        if (!endPoint.isEnable()) {
            PrettyPrinter.info("[{}] is disabled, skipped", endPoint.getSheetName());
            return;
        }

        //Initial sheet values
        final List<RedepositIeDTO> metaDataList;
        final Map<String, RedepositIeDTO> metaDataMap = new HashMap<>();
        try {
            metaDataList = RedepositIESheetsParser.parse(endPoint.getSheetName());

            metaDataList.forEach(dto -> {
                metaDataMap.put(dto.getOriginalPID(), dto);
            });

            metaDataList.clear();
        } catch (IOException e) {
            PrettyPrinter.error(log, "Failed to parse values in the excel file, [{}]", endPoint.getSheetName());
            return;
        }

        //Initial Template
        final Template metsTemplate;
        try {
            metsTemplate = MetsTemplateService.loadTemplate(endPoint.getMetsTemplateFileName());
        } catch (IOException e) {
            PrettyPrinter.error(log, "Failed to initial template [{}]", endPoint.getMetsTemplateFileName());
            return;
        }

        //Scan the IEs to be redeposited to Rosetta
        File srcDirPath = new File(endPoint.getSrcDir());
        if (!srcDirPath.exists()) {
            PrettyPrinter.error(log, "The src directory does not exist: {}", endPoint.getSrcDir());
        }

        File destDirPath = new File(endPoint.getDestDir());
        if (!destDirPath.exists()) {
            destDirPath.mkdirs();
            PrettyPrinter.info(log, "The dest directory is made: {}", endPoint.getDestDir());
        }

        File[] ieFolders = srcDirPath.listFiles();
        assert ieFolders != null;

        ResultOverview overview = new ResultOverview();
        for (File srcDir : ieFolders) {
            RedepositIeDTO ieProp = metaDataMap.get(srcDir.getName());
            File destDir = MetsUtils.combinePath(endPoint.getDestDir(), srcDir.getName());
            RedepositIEMetsGenerationHandler handler = new RedepositIEMetsGenerationHandler(metsTemplate, srcDir.getAbsolutePath(), destDir.getAbsolutePath(), ieProp, endPoint.isReplace());
            MetadataRetVal retVal = handler.process();
            overview.addResultItem(retVal, srcDir);
            PrettyPrinter.printResult(retVal, srcDir.getAbsolutePath());
        }

        metaDataMap.clear();

        PrettyPrinter.println(overview.getSummaryInfo());
    }

    public MetsTemplateService getMetsTemplateService() {
        return MetsTemplateService;
    }

    public void setMetsTemplateService(MetsTemplateService metsTemplateService) {
        MetsTemplateService = metsTemplateService;
    }

    public void addEndPoint(RedepositIEEndPoint endPoint) {
        this.endPoints.add(endPoint);
    }
}
