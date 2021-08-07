package nz.govt.natlib.ajhr.proc.redeposit;

import freemarker.template.Template;
import nz.govt.natlib.ajhr.metadata.MetadataRetVal;
import nz.govt.natlib.ajhr.metadata.RedepositIeDTO;
import nz.govt.natlib.ajhr.metadata.ResultOverview;
import nz.govt.natlib.ajhr.proc.MetsTemplateService;
import nz.govt.natlib.ajhr.util.MetsUtils;
import nz.govt.natlib.ajhr.util.PrettyPrinter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RedepositIEFolderScanProcessor {
    private static final Logger log = LoggerFactory.getLogger(RedepositIEFolderScanProcessor.class);

    private MetsTemplateService MetsTemplateService;

    private final List<RedepositIEEndPoint> endPoints = new ArrayList<>();

    public void process() {
        for (RedepositIEEndPoint endPoint : endPoints) {
            PrettyPrinter.info("Is going to process: {}", endPoint.toString());
            process(endPoint);
        }
    }

    public void process(RedepositIEEndPoint endPoint) {
        if (!endPoint.isEnable()) {
            PrettyPrinter.info("[{}] is disabled, skipped", endPoint.getSheetName());
            return;
        } else {
            PrettyPrinter.info(">>>Start to process: {}", endPoint.getSheetName());
        }

        //Initial sheet values
        final List<RedepositIeDTO> metaDataList;
        //final Map<String, RedepositIeDTO> metaDataMap = new HashMap<>();
        try {
            metaDataList = RedepositIESheetsParser.parse(endPoint.getSheetName(), endPoint.isMultipleRowsExtension());
        } catch (IOException e) {
            PrettyPrinter.error(log, "Failed to parse values in the excel file, [{}]", endPoint.getSheetName());
            return;
        }

        //Initial Template
        final Template metsTemplate;
        try {
            metsTemplate = MetsTemplateService.loadTemplate(endPoint.getMetsTemplateFilePath());
        } catch (IOException e) {
            PrettyPrinter.error(log, e, "Failed to initial template [{}]", endPoint.getMetsTemplateFilePath().getAbsolutePath());
            return;
        }

        //Scan the IEs to be redeposited to Rosetta
        File rootSrcDirPath = new File(endPoint.getSrcDir());
        if (!rootSrcDirPath.exists()) {
            PrettyPrinter.error(log, "The src directory does not exist: {}", endPoint.getSrcDir());
            return;
        }

        File rootDestDirPath = new File(endPoint.getDestDir());
        if (!rootDestDirPath.exists()) {
            rootDestDirPath.mkdirs();
            PrettyPrinter.info(log, "The dest directory is made: {}", endPoint.getDestDir());
        }

        File[] ieFolders = rootSrcDirPath.listFiles();
        assert ieFolders != null;

        ResultOverview overview = new ResultOverview();

        for (RedepositIeDTO ieProp : metaDataList) {
            File srcDir = MetsUtils.combinePath(rootSrcDirPath, ieProp.getOriginalPID());
            if (!srcDir.exists() || !srcDir.isDirectory()) {
                PrettyPrinter.info("There is no related folder for [Original PID = {}] in the directory: [{}]", ieProp.getOriginalPID(), rootSrcDirPath.getAbsolutePath());
                overview.addResultItem(MetadataRetVal.SKIP, srcDir);
                continue;
            }

            File destDir = MetsUtils.combinePath(endPoint.getDestDir(), ieProp.getOriginalPID());
            RedepositIEMetsGenerationHandler handler = new RedepositIEMetsGenerationHandler(metsTemplate, srcDir.getAbsolutePath(), destDir.getAbsolutePath(), ieProp, endPoint.isForcedReplaced(), endPoint.isMultipleRowsExtension());
            MetadataRetVal retVal = MetadataRetVal.FAIL;
            try {
                retVal = handler.process();
            } catch (IOException e) {
                PrettyPrinter.error(log, "Failed to process: {}, error is: {}", srcDir.getAbsolutePath(), ExceptionUtils.getStackTrace(e));
            }
            overview.addResultItem(retVal, srcDir);
            PrettyPrinter.printResult(retVal, srcDir.getAbsolutePath());
        }

        metaDataList.clear();

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
