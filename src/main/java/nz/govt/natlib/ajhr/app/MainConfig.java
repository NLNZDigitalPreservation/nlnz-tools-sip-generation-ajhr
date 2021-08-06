package nz.govt.natlib.ajhr.app;

import nz.govt.natlib.ajhr.proc.MetsTemplateService;
import nz.govt.natlib.ajhr.proc.ajhr.AJHRMetsFolderScanProcessor;
import nz.govt.natlib.ajhr.proc.ajhr.AJHTConfProperties;
import nz.govt.natlib.ajhr.proc.redeposit.RedepositIEEndPoint;
import nz.govt.natlib.ajhr.proc.redeposit.RedepositIEFolderScanProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class MainConfig {
    @Value("${Redeposit.Names}")
    private String[] redepositNames;

    @Autowired
    private MetsTemplateService metsTemplateService;

    @Bean
    public AJHRMetsFolderScanProcessor ajhrMetsFolderScanProcessor() {
        AJHTConfProperties prop = null;
        try {
            prop = AJHTConfProperties.getInstance();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new AJHRMetsFolderScanProcessor(prop, metsTemplateService);
    }

    @Bean
    public RedepositIEFolderScanProcessor redepositIEFolderScanProcessor() {
        RedepositIEFolderScanProcessor bean = new RedepositIEFolderScanProcessor();
        bean.setMetsTemplateService(metsTemplateService);

        for (String name : redepositNames) {
            try {
                RedepositIEEndPoint endPointOneOffIE = RedepositIEEndPoint.getInstance(name);
                bean.addEndPoint(endPointOneOffIE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bean;
    }
}
