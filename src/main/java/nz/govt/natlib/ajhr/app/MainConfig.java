package nz.govt.natlib.ajhr.app;

import nz.govt.natlib.ajhr.proc.MetsTemplateService;
import nz.govt.natlib.ajhr.proc.ajhr.AJHRMetsFolderScanProcessor;
import nz.govt.natlib.ajhr.proc.redeposit.RedepositIEEndPoint;
import nz.govt.natlib.ajhr.proc.redeposit.RedepositIEFolderScanProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

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

    @Value("${Redeposit.Names}")
    private String[] redepositNames;

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

        for (String name:redepositNames){
            try {
                RedepositIEEndPoint endPointOneOffIE =RedepositIEEndPoint.getInstance(name);
                bean.addEndPoint(endPointOneOffIE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bean;
    }
}
