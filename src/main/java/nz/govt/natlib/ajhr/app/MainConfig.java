package nz.govt.natlib.ajhr.app;

import nz.govt.natlib.ajhr.proc.MetsTemplateService;
import nz.govt.natlib.ajhr.proc.ajhr.AJHRMetsFolderScanProcessor;
import nz.govt.natlib.ajhr.proc.redeposit.RedepositIEEndPoint;
import nz.govt.natlib.ajhr.proc.redeposit.RedepositIEFolderScanProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MainConfig {
    @Value("${AJHR.enable}")
    private boolean enableAJHR;
    @Value("${AJHR.srcDir}")
    private String srcDirAJHR;
    @Value("${AJHR.destDir}")
    private String destDirAJHR;
    @Value("${AJHR.maxThreads}")
    private int maxThreadsAJHR;
    @Value("${AJHR.isForcedReplaced}")
    private boolean isForcedReplacedAJHR;
    @Value("${AJHR.startYear}")
    private int startYearAJHR;
    @Value("${AJHR.endYear}")
    private int endYearAJHR;

    @Value("${OneoffIE.enable}")
    private boolean enableOneoffIE;
    @Value("${OneoffIE.srcDir}")
    private String srcDirOneoffIE;
    @Value("${OneoffIE.destDir}")
    private String destDirOneoffIE;
    @Value("${OneoffIE.sheetName}")
    private String sheetNameOneoffIE;
    @Value("${OneoffIE.metsTemplate}")
    private String metsTemplateOneoffIE;
    @Value("${OneoffIE.isForcedReplaced}")
    private boolean isForcedReplacedOneoffIE;
    @Value("${OneoffIE.isMultipleRowsExtension}")
    private boolean isMultipleRowsExtensionOneoffIE;

    @Autowired
    private MetsTemplateService metsTemplateService;

    @Bean
    public AJHRMetsFolderScanProcessor ajhrMetsFolderScanProcessor() {
        return new AJHRMetsFolderScanProcessor(enableAJHR, srcDirAJHR, destDirAJHR, maxThreadsAJHR, isForcedReplacedAJHR, startYearAJHR, endYearAJHR, metsTemplateService);
    }

    @Bean
    public RedepositIEFolderScanProcessor redepositIEFolderScanProcessor() {
        RedepositIEFolderScanProcessor bean = new RedepositIEFolderScanProcessor();
        bean.setMetsTemplateService(metsTemplateService);

        RedepositIEEndPoint endPointOneOffIE = new RedepositIEEndPoint();
        endPointOneOffIE.setEnable(enableOneoffIE);
        endPointOneOffIE.setSrcDir(srcDirOneoffIE);
        endPointOneOffIE.setDestDir(destDirOneoffIE);
        endPointOneOffIE.setSheetName(sheetNameOneoffIE);
        endPointOneOffIE.setMetsTemplateFileName(metsTemplateOneoffIE);
        endPointOneOffIE.setForcedReplaced(isForcedReplacedOneoffIE);
        endPointOneOffIE.setMultipleRowsExtension(isMultipleRowsExtensionOneoffIE);

        bean.addEndPoint(endPointOneOffIE);
        return bean;
    }
}
