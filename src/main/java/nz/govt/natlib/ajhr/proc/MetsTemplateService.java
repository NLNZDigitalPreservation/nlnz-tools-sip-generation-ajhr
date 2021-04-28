package nz.govt.natlib.ajhr.proc;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import nz.govt.natlib.ajhr.util.PrettyPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class MetsTemplateService {
    private static final Logger log = LoggerFactory.getLogger(MetsTemplateService.class);

    private static final String ROOT_DIR_TEMPLATE = ".";
    private static final String NAME_DEFAULT_TEMPLATE = "mets-template.xml";

    public Configuration initConfiguration() throws IOException {
        // Create your Configuration instance, and specify if up to what FreeMarker
        // version (here 2.3.30) do you want to apply the fixes that are not 100%
        // backward-compatible. See the Configuration JavaDoc for details.
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_0);

        // Specify the source where the template files come from. Here I set a
        // plain directory for it, but non-file-system sources are possible too:
        cfg.setDirectoryForTemplateLoading(new File(ROOT_DIR_TEMPLATE));

        // From here we will set the settings recommended for new projects. These
        // aren't the defaults for backward compatibility.

        // Set the preferred charset template files are stored in. UTF-8 is
        // a good choice in most applications:
        cfg.setDefaultEncoding("UTF-8");

        // Sets how errors will appear.
        // During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        // Don't log exceptions inside FreeMarker that it will thrown at you anyway:
        cfg.setLogTemplateExceptions(false);

        // Wrap unchecked exceptions thrown during template processing into TemplateException-s:
        cfg.setWrapUncheckedExceptions(true);

        // Do not fall back to higher scopes when reading a null loop variable:
        cfg.setFallbackOnNullLoopVariable(false);

        return cfg;
    }

    public Template loadTemplate() throws IOException {
        return loadTemplate(NAME_DEFAULT_TEMPLATE);
    }

    public Template loadTemplate(final String templateName) throws IOException {
        Configuration cfg = initConfiguration();
        Resource resource = new ClassPathResource(templateName);
        InputStreamReader reader = new InputStreamReader(resource.getInputStream());

        Template template = null;
        try {
            template = new Template(templateName, reader, cfg);
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                PrettyPrinter.info(log, e.getMessage());
            }
        }

        return template;
    }
}
